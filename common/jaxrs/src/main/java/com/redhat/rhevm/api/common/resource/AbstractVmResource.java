/*
 * Copyright © 2010 Red Hat, Inc.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.redhat.rhevm.api.common.resource;

import java.net.URI;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import com.redhat.rhevm.api.common.util.ReapedMap;
import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.resource.ActionResource;

import com.redhat.rhevm.api.resource.VmResource;

public abstract class AbstractVmResource implements VmResource {

    private static final long REAP_AFTER = 2 * 60 * 60 * 1000L; // 2 hours

    private static ReapedMap.ValueToKeyMapper<String, ActionResource> KEY_MAPPER =
        new ReapedMap.ValueToKeyMapper<String, ActionResource>() {
            public String getKey(ActionResource value) {
                return value.getAction().getId();
            }
        };

    protected static Runnable DO_NOTHING = new Runnable() { public void run(){} };

    protected ReapedMap<String, ActionResource> actions;
    protected String id;

    public AbstractVmResource(String id) {
        this.id = id;
        actions = new ReapedMap<String, ActionResource>(KEY_MAPPER, REAP_AFTER);
    }

    public Response doAction(UriInfo uriInfo, Action action, final Runnable task) {
        Response.Status status = null;
        final ActionResource actionResource = new BaseActionResource(uriInfo, action);
        if (action.isAsync()) {
            action.setStatus(com.redhat.rhevm.api.model.Status.PENDING);
            actions.put(action.getId(), actionResource);
            // FIXME: use executor
            new Thread(new AbstractActionTask(action) {
                public void run() {
                    perform(action, task);
                    actions.reapable(KEY_MAPPER.getKey(actionResource));
                }
            }).start();
            status = Status.ACCEPTED;
        } else {
            // no need for self link in action if synchronous (as no querying
            // will ever be needed)
            //
            perform(action, task);
            status = Status.OK;
        }

        return Response.status(status).entity(action).build();
    }

    @Override
    public ActionResource getActionSubresource(String action, String oid) {
        // redirect back to the target VM if action no longer cached
        // REVISIT: ultimately we should look at redirecting
        // to the event/audit log
        //
        ActionResource exists = actions.get(oid);
        return exists != null
               ? exists
               : new ActionResource() {
                    @Override
                    public Response get(UriInfo uriInfo) {
                        URI redirect = uriInfo.getBaseUriBuilder().path("/vms" + id).build();
                        Response.Status status = Response.Status.MOVED_PERMANENTLY;
                        return Response.status(status).location(redirect).build();
                    }
                    @Override
                    public Action getAction() {
                        return null;
                    }
                };
    }

    private void perform(Action action, Runnable task) {
        action.setStatus(com.redhat.rhevm.api.model.Status.IN_PROGRESS);
        if (action.getGracePeriod() != null) {
            try {
                Thread.sleep(action.getGracePeriod().getExpiry());
            } catch (Exception e) {
                // ignore
            }
        }
        task.run();
        action.setStatus(com.redhat.rhevm.api.model.Status.COMPLETE);
    }

    private abstract class AbstractActionTask implements Runnable {
        protected Action action;
        AbstractActionTask(Action action) {
            this.action = action;
        }
    }
}

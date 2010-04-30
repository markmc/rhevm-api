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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.resource.ActionResource;

import com.redhat.rhevm.api.resource.VmResource;

public abstract class AbstractVmResource implements VmResource {

    protected static Runnable DO_NOTHING = new Runnable() { public void run(){} };

    // FIXME: replace with reapable map
    private Map<String, ActionResource> actions =
        Collections.synchronizedMap(new HashMap<String, ActionResource>());

    protected String id;

    public AbstractVmResource(String id) {
        this.id = id;
    }

    public Response doAction(UriInfo uriInfo, Action action, final Runnable task) {
        Response.Status status = null;
        ActionResource actionResource = new BaseActionResource(uriInfo, action);
        if (action.isAsync()) {
            action.setStatus(com.redhat.rhevm.api.model.Status.PENDING);
            actions.put(action.getId(), actionResource);
            // FIXME: use executor
            new Thread(new AbstractActionTask(action) {
                public void run() {
                    perform(action, task);
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
        ActionResource exists = actions.get(oid);
        return exists != null
               ? exists
               : new ActionResource() {
                    @Override
                    public Response get(UriInfo uriInfo) {
                        // redirect back to the target VM
                        // REVISIT: ultimately we should look at redirecting
                        // to the event/audit log
                        //
                        URI redirect = uriInfo.getBaseUriBuilder().path("/vms" + id).build();
                        Response.Status status = Response.Status.MOVED_PERMANENTLY;
                        return Response.status(status).location(redirect).build();
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
        action.setStatus(com.redhat.rhevm.api.model.Status.COMPLETE);
            task.run();
    }

    private abstract class AbstractActionTask implements Runnable {
        protected Action action;
        AbstractActionTask(Action action) {
            this.action = action;
        }
    }
}

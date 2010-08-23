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
package com.redhat.rhevm.api.mock.resource;

import java.util.concurrent.Executor;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.model.HostStatus;
import com.redhat.rhevm.api.resource.HostResource;
import com.redhat.rhevm.api.resource.HostNicsResource;
import com.redhat.rhevm.api.common.util.JAXBHelper;
import com.redhat.rhevm.api.common.util.LinkHelper;


public class MockHostResource extends AbstractMockResource<Host> implements HostResource {
    /* FIXME: would like to do:
     * private @Context UriInfo uriInfo;
     */

    /**
     * Package-protected ctor, never needs to be instantiated by JAX-RS framework.
     *
     * @param host     encapsulated host
     * @param executor executor used for asynchronous actions
     */
    MockHostResource(String id, Executor executor) {
        super(id, executor);
    }

    // FIXME: this needs to be atomic
    public void updateModel(Host host) {
        // update writable fields only
        if (host.isSetName()) {
            getModel().setName(host.getName());
        }
        if (host.isSetDescription()) {
            getModel().setDescription(host.getDescription());
        }
    }

    public Host addLinks() {
        return LinkHelper.addLinks(JAXBHelper.clone(OBJECT_FACTORY.createHost(getModel())));
    }

    /* FIXME: kill uriInfo param, make href auto-generated? */
    @Override
    public Host get(UriInfo uriInfo) {
        return addLinks();
    }

    @Override
    public Host update(UriInfo uriInfo, Host host) {
        validateUpdate(host);
        updateModel(host);
        return addLinks();
    }

    @Override
    public Response approve(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, new DoNothingTask(action));
    }

    @Override
    public Response install(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, new DoNothingTask(action));
    }

    @Override
    public Response activate(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, new HostStatusSetter(action, HostStatus.UP));
    }

    @Override
    public Response deactivate(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, new HostStatusSetter(action, HostStatus.MAINTENANCE));
    }

    @Override
    public Response commitNetConfig(UriInfo uriInfo, Action action) {
        return null;
    }

    private class HostStatusSetter extends AbstractActionTask {
        private HostStatus status;
        HostStatusSetter(Action action, HostStatus status) {
            super(action, "Host status failed with {0}");
            this.status = status;
        }
        public void execute() {
            if (status.equals(MockHostResource.this.getModel().getStatus())) {
                throw new IllegalStateException("Host status already: " + status);
            }
            MockHostResource.this.getModel().setStatus(status);
        }
    }

    @Override public HostNicsResource getHostNicsResource() { return null; }
}

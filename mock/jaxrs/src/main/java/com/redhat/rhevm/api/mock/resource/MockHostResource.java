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

import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.model.HostState;
import com.redhat.rhevm.api.model.HostStatus;
import com.redhat.rhevm.api.resource.AssignedPermissionsResource;
import com.redhat.rhevm.api.resource.AssignedTagsResource;
import com.redhat.rhevm.api.resource.HostResource;
import com.redhat.rhevm.api.resource.HostNicsResource;
import com.redhat.rhevm.api.resource.HostStorageResource;
import com.redhat.rhevm.api.resource.StatisticsResource;
import com.redhat.rhevm.api.common.resource.UriInfoProvider;
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
    MockHostResource(String id, Executor executor, UriInfoProvider uriProvider) {
        super(id, executor, uriProvider);
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
        return LinkHelper.addLinks(getUriInfo(), JAXBHelper.clone(OBJECT_FACTORY.createHost(getModel())));
    }

    /* FIXME: kill uriInfo param, make href auto-generated? */
    @Override
    public Host get() {
        return addLinks();
    }

    @Override
    public Host update(Host host) {
        validateUpdate(host);
        updateModel(host);
        return addLinks();
    }

    @Override
    public Response approve(Action action) {
        return doAction(getUriInfo(), new DoNothingTask(action));
    }

    @Override
    public Response install(Action action) {
        return doAction(getUriInfo(), new DoNothingTask(action));
    }

    @Override
    public Response fence(Action action) {
        return doAction(getUriInfo(), new DoNothingTask(action));
    }

    @Override
    public Response activate(Action action) {
        return doAction(getUriInfo(), new HostStateSetter(action, HostState.ACTIVE));
    }

    @Override
    public Response deactivate(Action action) {
        return doAction(getUriInfo(), new HostStateSetter(action, HostState.INACTIVE));
    }

    @Override
    public Response commitNetConfig(Action action) {
        return null;
    }

    @Override
    public Response iscsiDiscover(Action action) {
        return null;
    }

    @Override
    public Response iscsiLogin(Action action) {
        return null;
    }

    private class HostStateSetter extends AbstractActionTask {
        private HostState state;
        HostStateSetter(Action action, HostState state) {
            super(action, "Host state failed with {0}");
            this.state = state;
        }
        public void execute() {
            if (MockHostResource.this.getModel().isSetStatus() &&
                state.equals(MockHostResource.this.getModel().getStatus().getState())) {
                throw new IllegalStateException("Host state already: " + state);
            }
            MockHostResource.this.getModel().setStatus(new HostStatus());
            MockHostResource.this.getModel().getStatus().setState(state);
        }
    }

    @Override public HostNicsResource     getHostNicsResource()    { return null; }
    @Override public HostStorageResource  getHostStorageResource() { return null; }
    @Override public AssignedTagsResource getTagsResource()        { return null; }
    @Override public AssignedPermissionsResource getPermissionsResource() { return null; }
    @Override public StatisticsResource getStatisticsResource()    { return null; }

}

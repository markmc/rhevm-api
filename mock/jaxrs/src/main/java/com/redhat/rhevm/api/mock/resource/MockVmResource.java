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
import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.resource.AssignedPermissionsResource;
import com.redhat.rhevm.api.resource.AssignedTagsResource;
import com.redhat.rhevm.api.resource.AttachedUsersResource;
import com.redhat.rhevm.api.resource.DevicesResource;
import com.redhat.rhevm.api.resource.SnapshotsResource;
import com.redhat.rhevm.api.resource.VmResource;
import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.common.util.JAXBHelper;
import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.mock.model.MockVmStatus;


public class MockVmResource extends AbstractMockResource<VM> implements VmResource {
    /* FIXME: would like to do:
     * private @Context UriInfo uriInfo;
     */

    @SuppressWarnings("unused")
    private MockVmStatus status;

    /**
     * Package-protected ctor, never needs to be instantiated by JAX-RS framework.
     * @param executor executor used for asynchronous actions
     * @param vm       encapsulated VM
     */
    public MockVmResource(String id, Executor executor, UriInfoProvider uriProvider) {
        super(id, executor, uriProvider);
    }

    // FIXME: this needs to be atomic
    public void updateModel(VM vm) {
        // update writable fields only
        if (vm.isSetName()) {
            getModel().setName(vm.getName());
        }
        if (vm.isSetDescription()) {
            getModel().setDescription(vm.getDescription());
        }
        if (vm.isSetCluster()) {
            Cluster cluster = new Cluster();
            cluster.setId(vm.getCluster().getId());
            getModel().setCluster(cluster);
        }
    }

    public VM addLinks() {
        return LinkHelper.addLinks(getUriInfo(), JAXBHelper.clone("vm", VM.class, getModel()));
    }

    /* FIXME: kill uriInfo param, make href auto-generated? */
    @Override
    public VM get() {
        return addLinks();
    }

    @Override
    public VM update(VM vm) {
        validateUpdate(vm);
        updateModel(vm);
        return addLinks();
    }

    protected String[] getStrictlyImmutable() {
        return addStrictlyImmutable("type");
    }

    @Override
    public Response start(Action action) {
        return doAction(getUriInfo(), new VmStatusSetter(action, MockVmStatus.UP));
    }


    @Override
    public Response stop(Action action) {
        return doAction(getUriInfo(), new VmStatusSetter(action, MockVmStatus.DOWN));

    }

    @Override
    public Response shutdown(Action action) {
        return doAction(getUriInfo(), new VmStatusSetter(action, MockVmStatus.DOWN));
    }

    @Override
    public Response suspend(Action action) {
        return doAction(getUriInfo(), new DoNothingTask(action));
    }

    @Override
    public Response detach(Action action) {
        return doAction(getUriInfo(), new DoNothingTask(action));
    }

    @Override
    public Response migrate(Action action) {
        String migrateToHostId = action.getHost().getId();
        return doAction(getUriInfo(), new DoNothingTask(action));
    }

    @Override
    public Response export(Action action) {
        return null;
    }

    @Override
    public Response ticket(Action action) {
        return doAction(getUriInfo(), new DoNothingTask(action));
    }

    private class VmStatusSetter extends AbstractActionTask {
        private MockVmStatus status;
        VmStatusSetter(Action action, MockVmStatus status) {
            super(action, "VM status failed with {0}");
            this.status = status;
        }
        public void execute() {
            if (status.equals(MockVmResource.this.status)) {
                throw new IllegalStateException("VM status already: " + status);
            }
            MockVmResource.this.status = status;
        }
    }

    @Override public DevicesResource       getCdRomsResource()    { return null; }
    @Override public DevicesResource       getDisksResource()     { return null; }
    @Override public DevicesResource       getNicsResource()      { return null; }
    @Override public SnapshotsResource     getSnapshotsResource() { return null; }
    @Override public AttachedUsersResource getUsersResource()     { return null; }
    @Override public AssignedPermissionsResource getPermissionsResource() { return null; }
    @Override public AssignedTagsResource  getTagsResource()      { return null; }
}

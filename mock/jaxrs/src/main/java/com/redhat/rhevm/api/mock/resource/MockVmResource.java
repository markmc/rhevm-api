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

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.ActionsBuilder;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.resource.VmResource;
import com.redhat.rhevm.api.common.util.JAXBHelper;
import com.redhat.rhevm.api.mock.model.MockVmStatus;


public class MockVmResource extends AbstractMockResource<VM> implements VmResource {
    /* FIXME: would like to do:
     * private @Context UriInfo uriInfo;
     */

    @SuppressWarnings("unused")
    private MockVmStatus status;

    /**
     * Package-protected ctor, never needs to be instantiated by JAX-RS framework.
     *
     * @param vm       encapsulated VM
     * @param executor executor used for asynchronous actions
     */
    public MockVmResource(String id, Executor executor) {
        super(id, executor);
    }

    // FIXME: this needs to be atomic
    public void updateModel(VM vm) {
        // update writable fields only
        getModel().setName(vm.getName());
    }

    public VM addLinks(UriBuilder uriBuilder) {
        VM vm = JAXBHelper.clone("vm", VM.class, getModel());

        vm.setHref(uriBuilder.build().toString());

        ActionsBuilder actionsBuilder = new ActionsBuilder(uriBuilder, VmResource.class);
        vm.setActions(actionsBuilder.build());

        return vm;
    }

    /* FIXME: kill uriInfo param, make href auto-generated? */
    @Override
    public VM get(UriInfo uriInfo) {
        return addLinks(uriInfo.getRequestUriBuilder());
    }

    @Override
    public VM update(HttpHeaders headers, UriInfo uriInfo, VM vm) {
        validateUpdate(vm, headers);
        updateModel(vm);
        return addLinks(uriInfo.getRequestUriBuilder());
    }

    @Override
    public Response start(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, new VmStatusSetter(action, MockVmStatus.UP));
    }


    @Override
    public Response stop(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, new VmStatusSetter(action, MockVmStatus.DOWN));

    }

    @Override
    public Response shutdown(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, new VmStatusSetter(action, MockVmStatus.DOWN));
    }

    @Override
    public Response suspend(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, new DoNothingTask(action));
    }

    @Override
    public Response restore(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, new DoNothingTask(action));
    }

    @Override
    public Response migrate(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, new DoNothingTask(action));
    }

    @Override
    public Response move(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, new DoNothingTask(action));
    }

    @Override
    public Response detach(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, new DoNothingTask(action));

    }

    @Override
    public Response changeCD(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, new DoNothingTask(action));

    }

    @Override
    public Response ejectCD(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, new DoNothingTask(action));
    }

    private class VmStatusSetter extends AbstractActionTask {
        private MockVmStatus status;
        VmStatusSetter(Action action, MockVmStatus status) {
            super(action);
            this.status = status;
        }
        public void run() {
            MockVmResource.this.status = status;
        }
    }
}

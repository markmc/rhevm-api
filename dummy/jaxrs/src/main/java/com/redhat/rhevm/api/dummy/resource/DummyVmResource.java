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
package com.redhat.rhevm.api.dummy.resource;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.common.resource.AbstractVmResource;
import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.ActionsBuilder;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.resource.VmResource;
import com.redhat.rhevm.api.dummy.model.DummyVmStatus;
import com.redhat.rhevm.api.dummy.model.DummyVM;

public class DummyVmResource extends AbstractVmResource {
    /* FIXME: would like to do:
     * private @Context UriInfo uriInfo;
     */

    private DummyVM vm;

    /**
     * Package-protected ctor, never needs to be instantiated by JAX-RS framework.
     *
     * @param vm  encapsulated VM
     */
    public DummyVmResource(DummyVM vm) {
        super(vm.jaxb.getId());
        this.vm = vm;
    }

    /**
     * Package-level accessor for encapsulated VM
     *
     * @return  encapsulated VM
     */
    public DummyVM getVM() {
        return vm;
    }

    public VM addLinks(UriBuilder uriBuilder) {
        ActionsBuilder actionsBuilder = new ActionsBuilder(uriBuilder, VmResource.class);
        return vm.getJaxb(uriBuilder, actionsBuilder);
    }

    /* FIXME: kill uriInfo param, make href auto-generated? */
    @Override
    public VM get(UriInfo uriInfo) {
        return addLinks(uriInfo.getRequestUriBuilder());
    }

    @Override
    public VM update(HttpHeaders headers, UriInfo uriInfo, VM vm) {
        validateUpdate(vm, this.vm.jaxb, headers);
        this.vm.update(vm);
        return addLinks(uriInfo.getRequestUriBuilder());
    }

    @Override
    public Response start(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, action, new VmStatusSetter(DummyVmStatus.UP));
    }


    @Override
    public Response stop(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, action, new VmStatusSetter(DummyVmStatus.DOWN));

    }

    @Override
    public Response shutdown(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, action, new VmStatusSetter(DummyVmStatus.DOWN));
    }

    @Override
    public Response suspend(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, action, DO_NOTHING);
    }

    @Override
    public Response restore(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, action, DO_NOTHING);
    }

    @Override
    public Response migrate(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, action, DO_NOTHING);
    }

    @Override
    public Response move(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, action, DO_NOTHING);
    }

    @Override
    public Response detach(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, action, DO_NOTHING);

    }

    @Override
    public Response changeCD(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, action, DO_NOTHING);

    }

    @Override
    public Response ejectCD(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, action, DO_NOTHING);
    }

    private class VmStatusSetter implements Runnable {
        private DummyVmStatus status;
        VmStatusSetter(DummyVmStatus status) {
            this.status = status;
        }
        public void run() {
            vm.setStatus(status);
        }
    }
}

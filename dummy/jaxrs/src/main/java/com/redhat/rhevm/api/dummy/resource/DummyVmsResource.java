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

import java.util.HashMap;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.model.VMs;
import com.redhat.rhevm.api.resource.VmResource;
import com.redhat.rhevm.api.resource.VmsResource;
import com.redhat.rhevm.api.dummy.model.DummyVM;

public class DummyVmsResource implements VmsResource {
    /* REVISIT: Singleton lifecycle probably requires that UriInfo
     * must be modelled as a method parameter, as there would be
     * concurrency issues around injection into a data member
     */

    /* FIXME: synchronize access to this */
    private static HashMap<String, DummyVmResource> vms = new HashMap<String, DummyVmResource>();

    static {
        while (vms.size() < 10) {
            DummyVM vm = new DummyVM();
            vm.jaxb.setName("vm" + Integer.toString(vms.size()));
            vms.put(vm.jaxb.getId(), new DummyVmResource(vm));
        }
    }

    @Override
    public VMs list(UriInfo uriInfo) {
        VMs ret = new VMs();

        for (DummyVmResource vm : vms.values()) {
            String id = vm.getVM().getId();
            UriBuilder uriBuilder = uriInfo.getRequestUriBuilder().path(id);
            ret.getVMs().add(vm.addLinks(uriBuilder));
        }

        return ret;
    }

    @Override
    public Response add(UriInfo uriInfo, VM vm) {
        DummyVmResource newVM = new DummyVmResource(new DummyVM(vm));

        String id = newVM.getVM().getId();
        vms.put(id, newVM);

        UriBuilder uriBuilder = uriInfo.getRequestUriBuilder().path(id);

        VM ret = newVM.addLinks(uriBuilder);

        return Response.created(uriBuilder.build()).entity(ret).build();
    }

    @Override
    public void remove(String id) {
        vms.remove(id);
    }

    @Override
    public VmResource getVmSubResource(UriInfo uriInfo, String id) {
        return vms.get(id);
    }
}

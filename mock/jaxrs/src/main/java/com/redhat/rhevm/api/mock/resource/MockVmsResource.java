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

import java.util.HashMap;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.model.VMs;
import com.redhat.rhevm.api.resource.VmResource;
import com.redhat.rhevm.api.resource.VmsResource;

import static com.redhat.rhevm.api.mock.resource.AbstractMockResource.allocateId;


public class MockVmsResource implements VmsResource {
    /* REVISIT: Singleton lifecycle probably requires that UriInfo
     * must be modelled as a method parameter, as there would be
     * concurrency issues around injection into a data member
     */

    /* FIXME: synchronize access to this */
    private static HashMap<String, MockVmResource> vms = new HashMap<String, MockVmResource>();

    static {
        while (vms.size() < 10) {
            MockVmResource resource = new MockVmResource(allocateId(VM.class));
            resource.getModel().setName("vm" + resource.getModel().getId());
            vms.put(resource.getModel().getId(), resource);
        }
    }

    @Override
    public VMs list(UriInfo uriInfo) {
        VMs ret = new VMs();

        for (MockVmResource vm : vms.values()) {
            String id = vm.getModel().getId();
            UriBuilder uriBuilder = uriInfo.getRequestUriBuilder().path(id);
            ret.getVMs().add(vm.addLinks(uriBuilder));
        }

        return ret;
    }

    @Override
    public Response add(UriInfo uriInfo, VM vm) {
        MockVmResource resource = new MockVmResource(allocateId(VM.class));

        resource.updateModel(vm);

        String id = resource.getId();
        vms.put(id, resource);

        UriBuilder uriBuilder = uriInfo.getRequestUriBuilder().path(id);

        vm = resource.addLinks(uriBuilder);

        return Response.created(uriBuilder.build()).entity(vm).build();
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

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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.redhat.rhevm.api.mock.util.SimpleQueryEvaluator;
import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.model.VMs;
import com.redhat.rhevm.api.model.VmType;
import com.redhat.rhevm.api.resource.VmResource;
import com.redhat.rhevm.api.resource.VmsResource;

import static com.redhat.rhevm.api.mock.resource.AbstractMockResource.allocateId;


public class MockVmsResource extends AbstractMockQueryableResource<VM> implements VmsResource {

    private static Map<String, MockVmResource> vms =
        Collections.synchronizedMap(new HashMap<String, MockVmResource>());

    public MockVmsResource() {
        super(new SimpleQueryEvaluator<VM>());
    }

    public void populate() {
        synchronized (vms) {
            while (vms.size() < 10) {
                MockVmResource vmResource = new MockVmResource(allocateId(VM.class), getExecutor(), this);
                vmResource.getModel().setName("vm" + vmResource.getModel().getId());
                vmResource.getModel().setType(VmType.SERVER.value());
                vmResource.getModel().setTemplate(new Template());
                vmResource.getModel().getTemplate().setId(allocateId(Template.class));
                vms.put(vmResource.getModel().getId(), vmResource);
            }
        }
    }

    @Override
    public VMs list() {
        VMs ret = new VMs();

        for (MockVmResource vm : vms.values()) {
            if (filter(vm.getModel(), getUriInfo(), VM.class)) {
                ret.getVMs().add(vm.addLinks());
            }
        }

        return ret;
    }

    @Override
    public Response add(VM vm) {
        MockVmResource resource = new MockVmResource(allocateId(VM.class), getExecutor(), this);

        resource.updateModel(vm);

        String id = resource.getId();
        vms.put(id, resource);

        UriBuilder uriBuilder = getUriInfo().getAbsolutePathBuilder().path(id);

        vm = resource.addLinks();

        return Response.created(uriBuilder.build()).entity(vm).build();
    }

    @Override
    public void remove(String id) {
        vms.remove(id);
    }

    @Override
    public VmResource getVmSubResource(String id) {
        return vms.get(id);
    }
}

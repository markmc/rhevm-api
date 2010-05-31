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
package com.redhat.rhevm.api.powershell.resource;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.model.VMs;
import com.redhat.rhevm.api.resource.VmResource;
import com.redhat.rhevm.api.resource.VmsResource;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;


public class PowerShellVmsResource
    extends AbstractPowerShellCollectionResource<VM, PowerShellVmResource>
    implements VmsResource {

    @Override
    public VMs list(UriInfo uriInfo) {
        VMs ret = new VMs();
        for (VM vm : PowerShellVmResource.runAndParse(getSelectCommand("select-vm", uriInfo, VMs.class))) {
            UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(vm.getId());
            ret.getVMs().add(PowerShellVmResource.addLinks(PowerShellVmResource.addDevices(vm), uriInfo, uriBuilder));
        }
        return ret;
    }

    @Override
    public Response add(UriInfo uriInfo, VM vm) {
        StringBuilder buf = new StringBuilder();

        if (vm.getTemplateId() != null) {
            buf.append("$templ = get-template -templateid " + vm.getTemplateId() + "\n");
        }

        buf.append("add-vm");

        buf.append(" -name " + vm.getName());

        if (vm.getDescription() != null) {
            buf.append(" -description " + vm.getDescription());
        }

        if (vm.getTemplateId() != null) {
            buf.append(" -templateobject $templ");
        }

        if (vm.getCluster() != null) {
            buf.append(" -hostclusterid " + vm.getCluster().getId());
        }

        vm = PowerShellVmResource.runAndParseSingle(buf.toString());

        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(vm.getId());

        vm = PowerShellVmResource.addLinks(PowerShellVmResource.addDevices(vm), uriInfo, uriBuilder);

        return Response.created(uriBuilder.build()).entity(vm).build();
    }

    @Override
    public void remove(String id) {
        PowerShellCmd.runCommand("remove-vm -vmid " + id);
        removeSubResource(id);
    }

    @Override
    public VmResource getVmSubResource(UriInfo uriInfo, String id) {
        return getSubResource(id);
    }

    @Override
    protected PowerShellVmResource createSubResource(String id) {
        return new PowerShellVmResource(id, getExecutor());
    }
}

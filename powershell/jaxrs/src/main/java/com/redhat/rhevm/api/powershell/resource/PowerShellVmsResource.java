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

import com.redhat.rhevm.api.common.util.ReapedMap;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.model.VMs;
import com.redhat.rhevm.api.resource.VmResource;
import com.redhat.rhevm.api.resource.VmsResource;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;


public class PowerShellVmsResource implements VmsResource {
    /* FIXME: would like to do:
     * private @Context UriInfo uriInfo;
     */

    private ReapedMap<String, PowerShellVmResource> vms;

    public PowerShellVmsResource() {
        vms = new ReapedMap<String, PowerShellVmResource>();
    }

    @Override
    public VMs list(UriInfo uriInfo) {
        VMs ret = new VMs();
        for (VM vm : PowerShellVmResource.runAndParse("select-vm")) {
            UriBuilder uriBuilder = uriInfo.getRequestUriBuilder().path(vm.getId());
            ret.getVMs().add(PowerShellVmResource.addLinks(vm, uriBuilder));
        }
        return ret;
    }

/* FIXME: move this
    @Override
    public VMs search(String criteria) {
        return runAndParse("select-vm " + criteria);
    }
*/

    @Override
    public Response add(UriInfo uriInfo, VM vm) {
        StringBuilder buf = new StringBuilder();

        if (vm.getTemplateId() != null) {
            buf.append("$templ = get-template -templateid " + vm.getTemplateId() + "\n");
        }

        buf.append("add-vm");

        if (vm.getName() != null) {
            buf.append(" -name " + vm.getName());
        }

        if (vm.getTemplateId() != null) {
            buf.append(" -templateobject $templ");
        }

        if (vm.getClusterId() != null) {
            buf.append(" -hostclusterid " + vm.getClusterId());
        }

        vm = PowerShellVmResource.runAndParseSingle(buf.toString());

        UriBuilder uriBuilder = uriInfo.getRequestUriBuilder().path(vm.getId());

        vm = PowerShellVmResource.addLinks(vm, uriBuilder);

        return Response.created(uriBuilder.build()).entity(vm).build();
    }

    @Override
    public void remove(String id) {
        PowerShellCmd.runCommand("remove-vm -vmid " + id);
        removeSubResource(id);
    }

    @Override
    public VmResource getVmSubResource(UriInfo uriInfo, String id) {
        synchronized (vms) {
            PowerShellVmResource ret = vms.get(id);
            if (ret == null) {
                ret = new PowerShellVmResource(id);
                vms.put(id, ret);
                vms.reapable(id);
            }
            return ret;
        }
    }

    protected void removeSubResource(String id) {
        synchronized (vms) {
            vms.remove(id);
        }
    }
}

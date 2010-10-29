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

import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.redhat.rhevm.api.common.util.ReflectionHelper;
import com.redhat.rhevm.api.model.CpuTopology;
import com.redhat.rhevm.api.model.Display;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.model.VMs;
import com.redhat.rhevm.api.resource.VmResource;
import com.redhat.rhevm.api.resource.VmsResource;
import com.redhat.rhevm.api.powershell.model.PowerShellVM;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;

import static com.redhat.rhevm.api.common.util.CompletenessAssertor.validateParameters;

public class PowerShellVmsResource
    extends AbstractPowerShellCollectionResource<VM, PowerShellVmResource>
    implements VmsResource {

    static final String GET_STATS = "|foreach {$_;$_.getmemorystatistics();$_.getcpustatistics()}";

    public List<PowerShellVM> runAndParse(String command) {
        return PowerShellVmResource.runAndParse(getPool(), getParser(), command);
    }

    public PowerShellVM runAndParseSingle(String command) {
        return PowerShellVmResource.runAndParseSingle(getPool(), getParser(), command);
    }

    @Override
    public VMs list() {
        VMs ret = new VMs();
        for (PowerShellVM vm : runAndParse(getSelectCommand("select-vm", getUriInfo(), VM.class) + GET_STATS)) {
            ret.getVMs().add(PowerShellVmResource.addLinks(getUriInfo(), vm));
        }
        return ret;
    }

    @Override
    public Response add(VM vm) {
        validateParameters(vm, "name", "template.id|name", "cluster.id|name");
        StringBuilder buf = new StringBuilder();

        String templateArg = null;
        if (vm.getTemplate().isSetId()) {
            templateArg = PowerShellUtils.escape(vm.getTemplate().getId());
        } else {
            buf.append("$t = select-template -searchtext ");
            buf.append(PowerShellUtils.escape("name=" + vm.getTemplate().getName()));
            buf.append(";");
            templateArg = "$t.TemplateId";
        }

        String clusterArg = null;
        if (vm.getCluster().isSetId()) {
            clusterArg = PowerShellUtils.escape(vm.getCluster().getId());
        } else {
            buf.append("$c = select-cluster -searchtext ");
            buf.append(PowerShellUtils.escape("name=" +  vm.getCluster().getName()));
            buf.append(";");
            clusterArg = "$c.ClusterId";
        }

        buf.append("$templ = get-template -templateid " + templateArg + ";");

        buf.append("add-vm");

        buf.append(" -name " + PowerShellUtils.escape(vm.getName()) + "");
        if (vm.getDescription() != null) {
            buf.append(" -description " + PowerShellUtils.escape(vm.getDescription()));
        }
        if (vm.isSetHighlyAvailable() && vm.getHighlyAvailable().isValue()) {
            buf.append(" -highlyavailable $true");
            if (vm.getHighlyAvailable().isSetPriority()) {
                buf.append(" -priority " + Integer.toString(vm.getHighlyAvailable().getPriority()));
            }
        }
        if (vm.isSetDisplay()) {
            Display display = vm.getDisplay();
            if (display.isSetMonitors()) {
                buf.append(" -numofmonitors " + display.getMonitors());
            }
            if (display.isSetType()) {
                buf.append(" -displaytype " + PowerShellVM.asString(display.getType()));
            }
            // display port cannot be specified on creation, but the value
            // provided may in fact be correct (we won't know until we create
            // the VM) so for now we silently ignore a client-specified value
        }
        buf.append(" -templateobject $templ");
        buf.append(" -hostclusterid " + clusterArg);
        if (vm.getType() != null) {
            buf.append(" -vmtype " + ReflectionHelper.capitalize(vm.getType().toString().toLowerCase()));
        }
        if (vm.isSetMemory()) {
            buf.append(" -memorysize " + Math.round((double)vm.getMemory()/(1024*1024)));
        }
        if (vm.getCpu() != null && vm.getCpu().getTopology() != null) {
            CpuTopology topology = vm.getCpu().getTopology();
            buf.append(" -numofsockets " + topology.getSockets());
            buf.append(" -numofcpuspersocket " + topology.getCores());
        }
        String bootSequence = PowerShellVM.buildBootSequence(vm.getOs());
        if (bootSequence != null) {
            buf.append(" -defaultbootsequence " + bootSequence);
        }

        PowerShellVM ret = runAndParseSingle(buf.toString());

        vm = PowerShellVmResource.addLinks(getUriInfo(), ret);

        UriBuilder uriBuilder = getUriInfo().getAbsolutePathBuilder().path(ret.getId());

        return Response.created(uriBuilder.build()).entity(vm).build();
    }

    @Override
    public void remove(String id) {
        PowerShellCmd.runCommand(getPool(), "remove-vm -vmid " + PowerShellUtils.escape(id));
        removeSubResource(id);
    }

    @Override
    public VmResource getVmSubResource(String id) {
        return getSubResource(id);
    }

    @Override
    protected PowerShellVmResource createSubResource(String id) {
        return new PowerShellVmResource(id, getExecutor(), this, shellPools, getParser());
    }
}

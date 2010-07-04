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

import java.util.ArrayList;
import java.util.concurrent.Executor;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.CpuTopology;
import com.redhat.rhevm.api.model.Link;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.resource.VmResource;
import com.redhat.rhevm.api.common.util.JAXBHelper;
import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.powershell.model.PowerShellVM;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;


public class PowerShellVmResource extends AbstractPowerShellActionableResource<VM> implements VmResource {

    public PowerShellVmResource(String id, Executor executor, PowerShellPoolMap shellPools) {
        super(id, executor, shellPools);
    }

    public static ArrayList<PowerShellVM> runAndParse(PowerShellCmd shell, String command) {
        return PowerShellVM.parse(PowerShellCmd.runCommand(shell, command));
    }

    public static PowerShellVM runAndParseSingle(PowerShellCmd shell, String command) {
        ArrayList<PowerShellVM> vms = runAndParse(shell, command);

        return !vms.isEmpty() ? vms.get(0) : null;
    }

    public static VM addLinks(PowerShellVM vm) {
        VM ret = JAXBHelper.clone("vm", VM.class, vm);

        String [] deviceCollections = { "cdroms", "disks", "nics" };

        ret.getLinks().clear();

        for (String collection : deviceCollections) {
            Link link = new Link();
            link.setRel(collection);
            link.setHref(LinkHelper.getUriBuilder(ret).path(collection).build().toString());
            ret.getLinks().add(link);
        }

        return LinkHelper.addLinks(ret);
    }

    @Override
    public VM get(UriInfo uriInfo) {
        return addLinks(runAndParseSingle(getShell(), "get-vm " + PowerShellUtils.escape(getId())));
    }

    @Override
    public VM update(UriInfo uriInfo, VM vm) {
        validateUpdate(vm);

        StringBuilder buf = new StringBuilder();

        buf.append("$v = get-vm " + PowerShellUtils.escape(getId()) + "\n");

        if (vm.getName() != null) {
            buf.append("$v.name = " + PowerShellUtils.escape(vm.getName()) + "\n");
        }
        if (vm.getDescription() != null) {
            buf.append("$v.description = " + PowerShellUtils.escape(vm.getDescription()) + "\n");
        }
        if (vm.isSetMemory()) {
            buf.append(" $v.memorysize = " + Math.round((double)vm.getMemory()/(1024*1024)) + "\n");
        }
        if (vm.getCpu() != null && vm.getCpu().getTopology() != null) {
            CpuTopology topology = vm.getCpu().getTopology();
            buf.append(" $v.numofsockets = " + topology.getSockets() + "\n");
            buf.append(" $v.numofcpuspersocket = " + topology.getCores() + "\n");
        }
        String bootSequence = PowerShellVM.buildBootSequence(vm);
        if (bootSequence != null) {
            buf.append(" $v.defaultbootsequence = '" + bootSequence + "'\n");
        }

        buf.append("update-vm -vmobject $v");

        return addLinks(runAndParseSingle(getShell(), buf.toString()));
    }

    @Override
    public Response start(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, new CommandRunner(action, "start-vm", "vm", getId(), getShell()));
    }

    @Override
    public Response stop(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, new CommandRunner(action, "stop-vm", "vm", getId(), getShell()));
    }

    @Override
    public Response shutdown(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, new CommandRunner(action, "shutdown-vm", "vm", getId(), getShell()));
    }

    @Override
    public Response suspend(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, new CommandRunner(action, "suspend-vm", "vm", getId(), getShell()));
    }

    @Override
    public Response detach(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, new CommandRunner(action, "detach-vm", "vm", getId(), getShell()));
    }

    @Override
    public PowerShellCdRomsResource getCdRomsResource() {
        return new PowerShellCdRomsResource(getId(), shellPools);
    }

    @Override
    public PowerShellDisksResource getDisksResource() {
        return new PowerShellDisksResource(getId(), shellPools, "get-vm");
    }

    @Override
    public PowerShellNicsResource getNicsResource() {
        return new PowerShellNicsResource(getId(), shellPools, "get-vm");
    }
}

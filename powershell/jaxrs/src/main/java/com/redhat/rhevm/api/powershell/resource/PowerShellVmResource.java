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
import java.util.concurrent.Executor;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.CpuTopology;
import com.redhat.rhevm.api.model.Display;
import com.redhat.rhevm.api.model.Link;
import com.redhat.rhevm.api.model.Ticket;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.resource.VmResource;
import com.redhat.rhevm.api.common.util.JAXBHelper;
import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.common.util.ReflectionHelper;
import com.redhat.rhevm.api.powershell.model.PowerShellTicket;
import com.redhat.rhevm.api.powershell.model.PowerShellVM;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;

import static com.redhat.rhevm.api.common.util.CompletenessAssertor.validateParameters;

public class PowerShellVmResource extends AbstractPowerShellActionableResource<VM> implements VmResource {

    public PowerShellVmResource(String id,
                                Executor executor,
                                PowerShellPoolMap shellPools,
                                PowerShellParser parser) {
        super(id, executor, shellPools, parser);
    }

    public static List<PowerShellVM> runAndParse(PowerShellCmd shell, PowerShellParser parser, String command) {
        return PowerShellVM.parse(parser, PowerShellCmd.runCommand(shell, command));
    }

    public static PowerShellVM runAndParseSingle(PowerShellCmd shell, PowerShellParser parser, String command) {
        List<PowerShellVM> vms = runAndParse(shell, parser, command);

        return !vms.isEmpty() ? vms.get(0) : null;
    }

    public PowerShellVM runAndParseSingle(String command) {
        return runAndParseSingle(getShell(), getParser(), command);
    }

    public static VM addLinks(PowerShellVM vm) {
        VM ret = JAXBHelper.clone("vm", VM.class, vm);

        String [] deviceCollections = { "cdroms", "disks", "nics", "snapshots" };

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
        return addLinks(runAndParseSingle("get-vm " + PowerShellUtils.escape(getId())));
    }

    @Override
    public VM update(UriInfo uriInfo, VM vm) {
        validateUpdate(vm);

        StringBuilder buf = new StringBuilder();

        buf.append("$v = get-vm " + PowerShellUtils.escape(getId()) + ";");

        if (vm.getName() != null) {
            buf.append("$v.name = " + PowerShellUtils.escape(vm.getName()) + ";");
        }
        if (vm.getDescription() != null) {
            buf.append("$v.description = " + PowerShellUtils.escape(vm.getDescription()) + ";");
        }
        if (vm.getType() != null) {
            buf.append("$v.vmtype = " + ReflectionHelper.capitalize(vm.getType().toString().toLowerCase()) + ";");
        }
        if (vm.isSetMemory()) {
            buf.append(" $v.memorysize = " + Math.round((double)vm.getMemory()/(1024*1024)) + ";");
        }
        if (vm.getCpu() != null && vm.getCpu().getTopology() != null) {
            CpuTopology topology = vm.getCpu().getTopology();
            if (topology.isSetSockets()) {
                buf.append(" $v.numofsockets = " + topology.getSockets() + ";");
            }
            if (topology.isSetCores()) {
                buf.append(" $v.numofcpuspersocket = " + topology.getCores() + ";");
            }
        }
        String bootSequence = PowerShellVM.buildBootSequence(vm.getOs());
        if (bootSequence != null) {
            buf.append(" $v.defaultbootsequence = '" + bootSequence + "';");
        }
        if (vm.isSetDisplay()) {
            Display display = vm.getDisplay();
            if (display.isSetMonitors()) {
                buf.append(" $v.numofmonitors = " + display.getMonitors() + ";");
            }
            if (display.isSetType()) {
                buf.append(" $v.displaytype = '" + PowerShellVM.asString(display.getType()) + "';");
            }
            // REVISIT display port a read-only property => extend immutability
            // assertion to support "display.port" style syntax
        }

        buf.append("update-vm -vmobject $v");

        return addLinks(runAndParseSingle(buf.toString()));
    }

    protected String[] getStrictlyImmutable() {
        return addStrictlyImmutable("type");
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
    public Response migrate(UriInfo uriInfo, Action action) {
        validateParameters(action, "host.id|name");
        StringBuilder buf = new StringBuilder();

        String hostArg;
        if (action.getHost().isSetId()) {
            hostArg = PowerShellUtils.escape(action.getHost().getId());
        } else {
            buf.append("$h = select-host -searchtext ");
            buf.append(PowerShellUtils.escape("name=" + action.getHost().getName()));
            buf.append(";");
            hostArg = "$h.hostid";
        }

        buf.append("migrate-vm");
        buf.append(" -vmid " + PowerShellUtils.escape(getId()));
        buf.append(" -desthostid " + hostArg);

        return doAction(uriInfo, new CommandRunner(action, buf.toString(), getShell()));
    }

    @Override
    public Response ticket(UriInfo uriInfo, Action action) {
        StringBuilder buf = new StringBuilder();

        buf.append("set-vmticket");
        buf.append(" -vmid " + PowerShellUtils.escape(getId()));
        if (action.isSetTicket()) {
            Ticket ticket = action.getTicket();
            if (ticket.isSetValue()) {
                buf.append(" -ticket " + PowerShellUtils.escape(ticket.getValue()));
            }
            if (ticket.isSetExpiry()) {
                buf.append(" -validtime " + PowerShellUtils.escape(Long.toString(ticket.getExpiry())));
            }
        }

        return doAction(uriInfo,
                        new CommandRunner(action, buf.toString(), getShell()) {
                            protected void handleOutput(String output) {
                                action.setTicket(PowerShellTicket.parse(getParser(), output));
                            }
                        });
    }

    public class CdRomQuery extends PowerShellCdRomsResource.CdRomQuery {
        public CdRomQuery(String id) {
            super(id);
        }
        @Override protected String getCdIsoPath() {
            return runAndParseSingle("get-vm " + PowerShellUtils.escape(id)).getCdIsoPath();
        }
    }

    @Override
    public PowerShellCdRomsResource getCdRomsResource() {
        return new PowerShellCdRomsResource(getId(), shellPools, new CdRomQuery(getId()));
    }

    @Override
    public PowerShellDisksResource getDisksResource() {
        return new PowerShellDisksResource(getId(), shellPools, getParser(), "get-vm");
    }

    @Override
    public PowerShellNicsResource getNicsResource() {
        return new PowerShellNicsResource(getId(), shellPools, getParser(), "get-vm");
    }

    @Override
    public PowerShellSnapshotsResource getSnapshotsResource() {
        return new PowerShellSnapshotsResource(getId(), getExecutor(), shellPools, getParser());
    }
}

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

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.resource.VmResource;
import com.redhat.rhevm.api.common.resource.AbstractActionableResource;
import com.redhat.rhevm.api.powershell.model.PowerShellVM;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;


public class PowerShellVmResource extends AbstractActionableResource<VM> implements VmResource {
    /*
     * FIXME: would like to do: private @Context UriInfo uriInfo;
     */

    public PowerShellVmResource(String id, Executor executor) {
        super(id, executor);
    }

    public PowerShellVmResource(String id) {
        super(id);
    }

    public static ArrayList<VM> runAndParse(String command) {
        return PowerShellVM.parse(PowerShellCmd.runCommand(command));
    }

    public static VM runAndParseSingle(String command) {
        ArrayList<VM> vms = runAndParse(command);

        return !vms.isEmpty() ? vms.get(0) : null;
    }

    public static VM addLinks(VM vm, UriBuilder uriBuilder) {
        vm.setHref(uriBuilder.build().toString());
        return vm;
    }

    @Override
    public VM get(UriInfo uriInfo) {
        return addLinks(runAndParseSingle("get-vm " + getId()), uriInfo.getRequestUriBuilder());
    }

    @Override
    public VM update(HttpHeaders headers, UriInfo uriInfo, VM vm) {
        validateUpdate(vm, headers);

        StringBuilder buf = new StringBuilder();

        buf.append("$v = get-vm " + getId() + "\n");

        if (vm.getName() != null) {
            buf.append("$v.name = \"" + vm.getName() + "\"");
        }
        if (vm.getDescription() != null) {
            buf.append("$v.description = \"" + vm.getDescription() + "\"");
        }

        buf.append("\n");
        buf.append("update-vm -vmobject $v");

        return addLinks(runAndParseSingle(buf.toString()), uriInfo.getRequestUriBuilder());
    }

    @Override
    public Response start(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, new CommandRunner(action, "start-vm", "vm", getId()));
    }

    @Override
    public Response stop(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, new CommandRunner(action, "stop-vm", "vm", getId()));
    }

    @Override
    public Response shutdown(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, new CommandRunner(action, "shutdown-vm", "vm", getId()));
    }

    @Override
    public Response suspend(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, new CommandRunner(action, "suspend-vm", "vm", getId()));
    }

    @Override
    public Response restore(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, new CommandRunner(action, "restore-vm", "vm", getId()));
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
}

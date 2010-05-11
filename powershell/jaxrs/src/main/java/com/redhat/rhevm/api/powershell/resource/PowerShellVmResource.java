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

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.powershell.model.PowerShellVM;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
import com.redhat.rhevm.api.resource.VmResource;

public class PowerShellVmResource extends AbstractPowerShellResource<VM> implements VmResource {
    /*
     * FIXME: would like to do: private @Context UriInfo uriInfo;
     */

    public PowerShellVmResource(String id) {
        super(id);
    }

    public String getId() {
        return id;
    }

    public static ArrayList<VM> runAndParse(String command) {
        return PowerShellVM.parse(PowerShellUtils.runCommand(command));
    }

    public static VM runAndParseSingle(String command) {
        ArrayList<VM> vms = runAndParse(command);

        return !vms.isEmpty() ? vms.get(0) : null;
    }

    public static VM addLinks(VM vm, UriBuilder uriBuilder) {
        vm.setHref(uriBuilder.toString());
        return vm;
    }

    @Override
    public VM get(UriInfo uriInfo) {
        return setModel(addLinks(refreshRepresentation(),
                                 uriInfo.getRequestUriBuilder()));
    }

    @Override
    public VM update(HttpHeaders headers, UriInfo uriInfo, VM vm) {
        validateUpdate(vm, getModel(), headers);

        StringBuilder buf = new StringBuilder();

        buf.append("$v = get-vm " + id + "\n");

        if (vm.getName() != null) {
            buf.append("$v.name = \"" + vm.getName() + "\"");
        }

        buf.append("\n");
        buf.append("update-vm -vmobject $v");

        return setModel(addLinks(runAndParseSingle(buf.toString()),
                                 uriInfo.getRequestUriBuilder()));
    }

    @Override
    public Response start(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, action, new CommandRunner("start-vm"));
    }

    @Override
    public Response stop(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, action, new CommandRunner("stop-vm"));
    }

    @Override
    public Response shutdown(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, action, new CommandRunner("shutdown-vm"));
    }

    @Override
    public Response suspend(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, action, new CommandRunner("suspend-vm"));
    }

    @Override
    public Response restore(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, action, new CommandRunner("restore-vm"));
    }

    @Override
    public Response migrate(UriInfo uriInfo, Action action) {
        return doAction(uriInfo,action, DO_NOTHING);
    }

    @Override
    public Response move(UriInfo uriInfo, Action action) {
        return doAction(uriInfo,action, DO_NOTHING);
    }

    @Override
    public Response detach(UriInfo uriInfo, Action action) {
        return doAction(uriInfo,action, DO_NOTHING);
    }

    @Override
    public Response changeCD(UriInfo uriInfo, Action action) {
        return doAction(uriInfo,action, DO_NOTHING);
    }

    @Override
    public Response ejectCD(UriInfo uriInfo, Action action) {
        return doAction(uriInfo,action, DO_NOTHING);
    }

    protected VM refreshRepresentation() {
        return runAndParseSingle("get-vm " + id);
    }

    private class CommandRunner implements Runnable {
        private String command;
        CommandRunner(String command) {
            this.command = command;
        }
        public void run() {
            PowerShellUtils.runCommand(command + " -vmid " + id);
        }
    }

}

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
import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.resource.HostResource;
import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.powershell.model.PowerShellHost;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;


public class PowerShellHostResource extends AbstractPowerShellActionableResource<Host> implements HostResource {

    public PowerShellHostResource(String id, Executor executor, PowerShellPoolMap powerShellPoolMap) {
        super(id, executor, powerShellPoolMap);
    }

    /* needed because there are two get-host commands */
    private static final String CMD_PREFIX = "rhevmpssnapin\\";

    public static ArrayList<Host> runAndParse(PowerShellCmd shell, String command) {
        return PowerShellHost.parse(PowerShellCmd.runCommand(shell, command));
    }

    public static Host runAndParseSingle(PowerShellCmd shell, String command) {
        ArrayList<Host> hosts = runAndParse(shell, command);

        return !hosts.isEmpty() ? hosts.get(0) : null;
    }

    @Override
    public Host get(UriInfo uriInfo) {
        return LinkHelper.addLinks(runAndParseSingle(getShell(), CMD_PREFIX + "get-host " + PowerShellUtils.escape(getId())));
    }

    @Override
    public Host update(UriInfo uriInfo, Host host) {
        validateUpdate(host);

        StringBuilder buf = new StringBuilder();

        buf.append("$h = " + CMD_PREFIX + "get-host " + PowerShellUtils.escape(getId()) + "\n");

        if (host.getName() != null) {
            buf.append("$h.name = " + PowerShellUtils.escape(host.getName()) + "\n");
        }

        buf.append("update-host -hostobject $h");

        return LinkHelper.addLinks(runAndParseSingle(getShell(), buf.toString()));
    }

    @Override
    public Response approve(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, new CommandRunner(action, "approve-host", "host", getId(), getShell()));
    }

    @Override
    public Response install(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, new HostInstaller(action, action.getRootPassword(), getShell()));
    }

    @Override
    public Response activate(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, new CommandRunner(action, "resume-host", "host", getId(), getShell()));
    }

    @Override
    public Response deactivate(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, new CommandRunner(action, "suspend-host", "host", getId(), getShell()));
    }

    class HostInstaller extends AbstractPowerShellActionTask {

        private String rootPassword;
        private PowerShellCmd shell;

        HostInstaller(Action action, String rootPassword, PowerShellCmd shell) {
            super(action, "$h = " + CMD_PREFIX + "get-host ");
            this.rootPassword = rootPassword;
            this.shell = shell;
        }

        public void execute() {
            String id = PowerShellHostResource.this.getId();
            StringBuilder buf = new StringBuilder();

            buf.append(command + PowerShellUtils.escape(id) + "\n");

            buf.append("update-host");
            buf.append(" -hostobject $h");
            buf.append(" -install");
            buf.append(" -rootpassword " + PowerShellUtils.escape(rootPassword));

            PowerShellCmd.runCommand(shell, buf.toString());
        }
    }
}

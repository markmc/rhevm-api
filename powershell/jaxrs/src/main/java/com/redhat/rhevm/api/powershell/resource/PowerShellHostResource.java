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
import com.redhat.rhevm.api.model.ActionsBuilder;
import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.resource.HostResource;
import com.redhat.rhevm.api.common.resource.AbstractActionableResource;
import com.redhat.rhevm.api.powershell.model.PowerShellHost;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;

public class PowerShellHostResource extends AbstractActionableResource<Host> implements HostResource {

    public PowerShellHostResource(String id, Executor executor) {
        super(id, executor);
    }

    /* needed because there are two get-host commands */
    private static final String CMD_PREFIX = "rhevmpssnapin\\";

    public static ArrayList<Host> runAndParse(String command) {
        return PowerShellHost.parse(PowerShellCmd.runCommand(command));
    }

    public static Host runAndParseSingle(String command) {
        ArrayList<Host> hosts = runAndParse(command);

        return !hosts.isEmpty() ? hosts.get(0) : null;
    }

    public static Host addLinks(Host host, UriBuilder uriBuilder) {
        host.setHref(uriBuilder.build().toString());

        ActionsBuilder actionsBuilder = new ActionsBuilder(uriBuilder, HostResource.class);
        host.setActions(actionsBuilder.build());

        return host;
    }

    @Override
    public Host get(UriInfo uriInfo) {
        return addLinks(runAndParseSingle(CMD_PREFIX + "get-host " + getId()), uriInfo.getRequestUriBuilder());
    }

    @Override
    public Host update(HttpHeaders headers, UriInfo uriInfo, Host host) {
        validateUpdate(host, headers);

        StringBuilder buf = new StringBuilder();

        buf.append("$h = get-host " + getId() + "\n");

        if (host.getName() != null) {
            buf.append("$h.name = '" + host.getName() + "'");
        }

        buf.append("\n");
        buf.append("update-host -hostobject $h");

        return addLinks(runAndParseSingle(buf.toString()), uriInfo.getRequestUriBuilder());
    }

    @Override
    public Response approve(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, new CommandRunner(action, "approve-host", "host", getId()));
    }

    @Override
    public Response fence(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, new CommandRunner(action, "fence-host", "host", getId()));
    }

    @Override
    public Response resume(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, new CommandRunner(action, "resume-host", "host", getId()));
    }
}

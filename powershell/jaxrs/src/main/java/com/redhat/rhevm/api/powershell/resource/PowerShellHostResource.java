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

import java.net.URI;
import java.util.ArrayList;

import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.model.Link;
import com.redhat.rhevm.api.resource.HostResource;
import com.redhat.rhevm.api.powershell.model.PowerShellHost;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;

public class PowerShellHostResource implements HostResource {
    /* FIXME: would like to do:
     * private @Context UriInfo uriInfo;
     */

    private String id;

    public PowerShellHostResource(String id) {
        this.id = id;
    }

    /* needed because there are two get-host commands */
    private static final String CMD_PREFIX = "rhevmpssnapin\\";

    public static ArrayList<Host> runAndParse(String command) {
        return PowerShellHost.parse(PowerShellUtils.runCommand(command));
    }

    public static Host runAndParseSingle(String command) {
        ArrayList<Host> hosts = runAndParse(command);

        return !hosts.isEmpty() ? hosts.get(0) : null;
    }

    public static Host addLink(Host host, URI uri) {
        host.setLink(new Link("self", uri));
        return new Host(host);
    }

    @Override
    public Host get(UriInfo uriInfo) {
        return addLink(runAndParseSingle(CMD_PREFIX + "get-host " + id), uriInfo.getRequestUriBuilder().build());
    }

    @Override
    public Host update(UriInfo uriInfo, Host host) {
        StringBuilder buf = new StringBuilder();

        buf.append("$h = get-host " + id + "\n");

        if (host.getName() != null) {
            buf.append("$h.name = \"" + host.getName() + "\"");
        }

        buf.append("\n");
        buf.append("update-host -hostobject $v");

        return addLink(runAndParseSingle(buf.toString()), uriInfo.getRequestUriBuilder().build());
    }

    @Override
    public void approve() {
        PowerShellUtils.runCommand("approve-host -hostid " + id);
    }

    @Override
    public void fence() {
    }

    @Override
    public void resume() {
    }

/*
    @Override
    public void connectStorage(String id, String storageDevice) {
    }
*/
}

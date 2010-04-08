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
package com.redhat.rhevm.api.powershell;

import java.util.List;
import java.util.ArrayList;

import javax.ejb.Stateless;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.Host;
import com.redhat.rhevm.api.Hosts;

@Stateless
public class PowerShellHosts implements Hosts
{
	/* FIXME: would like to do:
         * private @Context UriInfo uriInfo;
         */

	// needed because there are two get-host commands
	private static final String CMD_PREFIX = "rhevmpssnapin\\";

	private void runCommand(String command) {
		PowerShellUtils.runCommand(command);
	}

	private ArrayList<Host> runAndParse(String command) {
		return PowerShellHost.parse(PowerShellUtils.runCommand(command));
	}

	private Host runAndParseSingle(String command) {
		ArrayList<Host> hosts = runAndParse(command);

		return !hosts.isEmpty() ? hosts.get(0) : null;
	}

	private Host addHref(Host host, UriInfo uriInfo) {
		host.setHref(uriInfo.getBaseUriBuilder().clone().path("hosts").path(host.getId()).build());
		return host;
	}

	private List<Host> addHrefs(List<Host> hosts, UriInfo uriInfo) {
		for (Host host : hosts)
			addHref(host, uriInfo);
		return hosts;
	}

	@Override
	public Host get(UriInfo uriInfo, String id) {
		List<Host> hosts = runAndParse(CMD_PREFIX + "get-host " + id);

		return hosts.isEmpty() ? null : addHref(hosts.get(0), uriInfo);
	}

	@Override
	public List<Host> list(UriInfo uriInfo) {
		return addHrefs(runAndParse("select-host"), uriInfo);
	}

/* FIXME: move this
	@Override
	public List<Host> search(String criteria) {
		return runAndParse("select-host " + criteria);
	}
*/

	@Override
	public Response add(UriInfo uriInfo, Host host) {
		StringBuilder buf = new StringBuilder();

		buf.append("add-host");

		if (host.getName() != null) {
			buf.append(" -name " + host.getName());
		}

		if (host.getAddress() != null) {
			buf.append(" -hostname " + host.getAddress());
		}

		if (host.getRootPassword() != null) {
			buf.append(" -rootpassword " + host.getRootPassword());
		}

		host = addHref(runAndParseSingle(buf.toString()), uriInfo);

		return Response.created(host.getHref()).entity(host).build();
	}

	@Override
	public Host update(UriInfo uriInfo, String id, Host host) {
		StringBuilder buf = new StringBuilder();

		buf.append("$h = get-host " + id + "\n");

		if (host.getName() != null) {
			buf.append("$h.name = \"" + host.getName() + "\"");
		}

		buf.append("\n");
		buf.append("update-host -hostobject $v");

		return addHref(runAndParseSingle(buf.toString()), uriInfo);
	}

	@Override
	public void remove(String id) {
		runCommand("remove-host -hostid " + id);
	}

	@Override
	public void approve(String id) {
		runCommand("approve-host -hostid " + id);
	}

	@Override
	public void fence(String id) {
	}

	@Override
	public void resume(String id) {
	}

/*
	@Override
	public void connectStorage(String id, String storageDevice) {
	}
*/
}

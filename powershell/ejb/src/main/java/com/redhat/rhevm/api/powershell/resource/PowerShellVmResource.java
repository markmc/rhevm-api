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
import java.util.List;
import java.util.ArrayList;

import javax.ejb.Stateless;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Link;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.model.VMs;
import com.redhat.rhevm.api.resource.VmResource;
import com.redhat.rhevm.api.powershell.model.PowerShellVM;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;

@Stateless
public class PowerShellVmResource implements VmResource
{
	/* FIXME: would like to do:
         * private @Context UriInfo uriInfo;
         */

	private void runCommand(String command) {
		PowerShellUtils.runCommand(command);
	}

	private ArrayList<VM> runAndParse(String command) {
		return PowerShellVM.parse(PowerShellUtils.runCommand(command));
	}

	private VM runAndParseSingle(String command) {
		ArrayList<VM> vms = runAndParse(command);

		return !vms.isEmpty() ? vms.get(0) : null;
	}

	private UriBuilder getUriBuilder(UriInfo uriInfo) {
		return uriInfo.getBaseUriBuilder().clone().path("vms");
	}

	private VM addLink(VM vm, URI uri) {
		vm.setLink(new Link("self", uri));
		return new VM(vm);
	}

	private VM addLink(VM vm, UriBuilder uriBuilder) {
		return addLink(vm, uriBuilder.clone().path(vm.getId()).build());
	}

	private VM addLink(VM vm, UriInfo uriInfo) {
		return addLink(vm, getUriBuilder(uriInfo));
	}

	private VMs addLinks(List<VM> vms, UriInfo uriInfo) {
		VMs ret = new VMs();
		for (VM vm : vms)
			ret.getVMs().add(addLink(vm, uriInfo));
		return ret;
	}

	@Override
	public VM get(UriInfo uriInfo, String id) {
		return addLink(runAndParseSingle("get-vm " + id), uriInfo);
	}

	@Override
	public VMs list(UriInfo uriInfo) {
		return addLinks(runAndParse("select-vm"), uriInfo);
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

		vm = runAndParseSingle(buf.toString());

		URI uri = getUriBuilder(uriInfo).clone().path(vm.getId()).build();

		return Response.created(uri).entity(addLink(vm, uri)).build();
	}

	@Override
	public VM update(UriInfo uriInfo, String id, VM vm) {
		StringBuilder buf = new StringBuilder();

		buf.append("$v = get-vm " + vm.getId() + "\n");

		if (vm.getName() != null) {
			buf.append("$v.name = \"" + vm.getName() + "\"");
		}

		buf.append("\n");
		buf.append("update-vm -vmobject $v");

		return addLink(runAndParseSingle(buf.toString()), uriInfo);
	}

	@Override
	public void remove(String id) {
		runCommand("remove-vm -vmid " + id);
	}

	@Override
	public void start(String id) {
		runCommand("start-vm -vmid ");
	}

	@Override
	public void stop(String id) {
		runCommand("stop-vm -vmid ");
	}

	@Override
	public void shutdown(String id) {
		runCommand("shutdown-vm -vmid ");
	}

	@Override
	public void suspend(String id) {
		runCommand("suspend-vm -vmid ");
	}

	@Override
	public void restore(String id) {
		runCommand("restore-vm -vmid ");
	}

	@Override
	public void migrate(String id) {
	}

	@Override
	public void move(String id) {
	}

	@Override
	public void detach(String id) {
	}

	@Override
	public void changeCD(String id) {
	}

	@Override
	public void ejectCD(String id) {
	}
}

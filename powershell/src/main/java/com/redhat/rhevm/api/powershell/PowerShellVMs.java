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
import javax.jws.WebService;

import com.redhat.rhevm.api.VM;
import com.redhat.rhevm.api.VMs;

@Stateless
@WebService
public class PowerShellVMs implements VMs
{
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

	@Override
	public VM get(String id) {
		return runAndParseSingle("get-vm " + id);
	}

	@Override
	public List<VM> list() {
		return runAndParse("select-vm");
	}

	@Override
	public List<VM> search(String criteria) {
		return runAndParse("select-vm " + criteria);
	}

	@Override
	public VM add(VM vm) {
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

		return runAndParseSingle(buf.toString());
	}

	@Override
	public VM update(VM vm) {
		StringBuilder buf = new StringBuilder();

		buf.append("$v = get-vm " + vm.getId() + "\n");

		if (vm.getName() != null) {
			buf.append("$v.name = \"" + vm.getName() + "\"");
		}

		buf.append("\n");
		buf.append("update-vm -vmobject $v");

		return runAndParseSingle(buf.toString());
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

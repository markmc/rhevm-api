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
package com.redhat.rhevm.api.powershell.model;

import java.util.ArrayList;
import java.util.HashMap;

import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.powershell.model.PowerShellVM;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;

public class PowerShellVM extends VM
{
	public PowerShellVM() {
	}

	public static ArrayList<VM> parse(String output) {
		ArrayList<HashMap<String,String>> vmsProps = PowerShellUtils.parseProps(output);
		ArrayList<VM> ret = new ArrayList<VM>();

		for (HashMap<String,String> props : vmsProps) {
			PowerShellVM vm = new PowerShellVM();

			vm.setId(props.get("vmid"));
			vm.setName(props.get("name"));

			ret.add(vm);
		}

		return ret;
	}
}

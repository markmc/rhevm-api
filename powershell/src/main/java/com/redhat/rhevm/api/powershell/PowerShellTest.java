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

import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;

import com.redhat.rhevm.api.Host;
import com.redhat.rhevm.api.VM;

public class PowerShellTest
{
	private final Log log = LogFactory.getLog(this.getClass());

	private Random rand = new Random();

	private void test() {
		PowerShellVMs vmsService = new PowerShellVMs();

		VM newVm = new VM();
		newVm.setName("baboon" + rand.nextInt(1000));
		newVm.setTemplateId("00000000-0000-0000-0000-000000000000");
		newVm.setClusterId("0");
		newVm = vmsService.add(newVm);
		log.info("added " + newVm.getId() + " " + newVm.getName());

		newVm.setName("prawn");
		newVm = vmsService.update(newVm);
		log.info("updated " + newVm.getId() + " " + newVm.getName());

		for (VM vm : vmsService.list()) {
			vm = vmsService.get(vm.getId());
			log.info("vm " + vm.getId() + " " + vm.getName());
		}

		vmsService.remove(newVm.getId());
		log.info("removed " + newVm.getId() + " " + newVm.getName());

		PowerShellHosts hostsService = new PowerShellHosts();

		for (Host host : hostsService.list()) {
			host = hostsService.get(host.getId());
			log.info("host " + host.getId());
		}
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();

		(new PowerShellTest()).test();
	}
}

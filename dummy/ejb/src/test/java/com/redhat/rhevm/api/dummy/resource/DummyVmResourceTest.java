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
package com.redhat.rhevm.api.dummy.resource;


import org.junit.Test;

import com.redhat.rhevm.api.model.Link;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.model.VMs;

public class DummyVmResourceTest extends DummyTestBase
{
	private DummyTestBase.VmsResource getService() {
		return createVmsResource(getEntryPoint("vms").getHref());
	}

	private void checkVM(VM vm) {
		assertNotNull(vm.getName());
		assertNotNull(vm.getId());
		assertNotNull(vm.getLink());
		assertNotNull(vm.getLink().getRel());
		assertNotNull(vm.getLink().getHref());
		assertTrue(vm.getLink().getHref().endsWith("/vms/" + vm.getId()));
		assertNotNull(vm.getActions());
		assertTrue(vm.getActions().getLinks().size() > 0);
		boolean includesStartLink = false;
		for (Link actionLink : vm.getActions().getLinks()) {
		    includesStartLink = actionLink.getHref().endsWith("/vms/" + vm.getId() + "/start");
		    if (includesStartLink) {
		        break;
		    }
		}
		assertTrue("expected start link", includesStartLink);
	}

	@Test
	public void testGetVmsList() throws Exception {
	    DummyTestBase.VmsResource service = getService();
		assertNotNull(service);

		VMs vms = service.list();
		assertNotNull(vms);
		assertTrue(vms.getVMs().size() > 0);

		for (VM vm : vms.getVMs()) {
			checkVM(vm);

			VM t = service.get(vm.getId());
			checkVM(t);
			assertEquals(vm.getId(), t.getId());
		}
	}	
}

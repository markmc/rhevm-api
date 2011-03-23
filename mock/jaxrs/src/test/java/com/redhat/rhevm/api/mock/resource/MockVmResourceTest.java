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
package com.redhat.rhevm.api.mock.resource;

import org.jboss.resteasy.client.ClientResponseFailure;
import org.junit.Test;

import com.redhat.rhevm.api.model.Fault;
import com.redhat.rhevm.api.model.Link;
import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.model.VMs;
import com.redhat.rhevm.api.model.VmType;

public class MockVmResourceTest extends MockTestBase {
    private MockTestBase.VmsResource getService() {
        return createVmsResource(getEntryPoint("vms").getHref());
    }

    private void checkVM(VM vm) {
        assertNotNull(vm.getName());
        assertNotNull(vm.getId());
        assertNotNull(vm.getHref());
        assertTrue(vm.getHref().endsWith("vms/" + vm.getId()));

        Template template = vm.getTemplate();
        assertNotNull(template);
        assertNotNull(template.getId());
        assertNotNull(template.getHref());
        assertTrue(template.getHref().endsWith("templates/" + template.getId()));

        assertNotNull(vm.getActions());
        assertTrue(vm.getActions().getLinks().size() > 0);
        boolean includesStartLink = false;
        for (Link actionLink : vm.getActions().getLinks()) {
            includesStartLink = actionLink.getHref().endsWith("vms/" + vm.getId() + "/start");
            if (includesStartLink) {
                break;
            }
        }
        assertTrue("expected start link", includesStartLink);
    }

    @Test
    public void testGetVmsList() throws Exception {
        MockTestBase.VmsResource service = getService();
        assertNotNull(service);

        VMs vms = service.list(null);
        assertNotNull(vms);
        assertTrue(vms.getVMs().size() > 0);

        for (VM vm : vms.getVMs()) {
            checkVM(vm);

            VM t = service.get(vm.getId());
            checkVM(t);
            assertEquals(vm.getId(), t.getId());
        }
    }

    @Test
    public void testGetVmsQuery() throws Exception {
        MockTestBase.VmsResource service = getService();
        assertNotNull(service);

        VMs vms = service.list("name=*1");
        assertNotNull(vms);
        assertEquals("unepected number of query matches", 1, vms.getVMs().size());

        VM vm = vms.getVMs().get(0);
        checkVM(vm);

        VM t = service.get(vm.getId());
        checkVM(t);
        assertEquals(vm.getId(), t.getId());
    }

    @Test
    public void testVmGoodUpdate() throws Exception {
        MockTestBase.VmsResource service = getService();
        assertNotNull(service);

        VM update = new VM();
        update.setName("wonga");
        update.setType(VmType.SERVER.value());
        VM updated = service.update("1", update);
        assertNotNull(updated);
        assertEquals(updated.getName(), "wonga");
        checkVM(updated);
    }

    @Test
    public void testVmBadIdUpdate() throws Exception {
        VM update = new VM();
        update.setId("fluffy");
        doTestVmBadUpdate(update, "id");
    }

    @Test
    public void testVmBadTypeUpdate() throws Exception {
        VM update = new VM();
        update.setType(VmType.DESKTOP.value());
        doTestVmBadUpdate(update, "type");
    }

    @SuppressWarnings("unchecked")
    private void doTestVmBadUpdate(VM update, String field) throws Exception {
        MockTestBase.VmsResource service = getService();
        assertNotNull(service);

        try {
            service.update("2", update);
            fail("expected ClientResponseFailure");
        } catch (ClientResponseFailure cfe) {
            assertEquals(409, cfe.getResponse().getStatus());
            Fault fault = (Fault)cfe.getResponse().getEntity(Fault.class);
            assertNotNull(fault);
            assertEquals("Broken immutability constraint", fault.getReason());
            assertEquals("Attempt to set immutable field: " + field, fault.getDetail());
        }
    }

}

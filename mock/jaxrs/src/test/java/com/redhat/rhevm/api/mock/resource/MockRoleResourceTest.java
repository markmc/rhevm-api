/*
 * Copyright © 2011 Red Hat, Inc.
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


import org.junit.Test;

import com.redhat.rhevm.api.model.Role;
import com.redhat.rhevm.api.model.Roles;

public class MockRoleResourceTest extends MockTestBase {
    private MockTestBase.RolesResource getService() {
        return createRolesResource(getEntryPoint("roles").getHref());
    }

    private void checkRole(Role role) {
        assertNotNull(role.getName());
        assertNotNull(role.getId());
        assertNotNull(role.getHref());
        assertTrue(role.getHref().endsWith("roles/" + role.getId()));
        assertNull(role.getActions());
    }

    @Test
    public void testGetRolesList() throws Exception {
        MockTestBase.RolesResource service = getService();
        assertNotNull(service);

        Roles roles = service.list(null);
        assertNotNull(roles);
        assertTrue(roles.getRoles().size() > 0);

        for (Role role : roles.getRoles()) {
            checkRole(role);

            Role r = service.get(role.getId());
            checkRole(r);
            assertEquals(role.getId(), r.getId());
        }
    }

    @Test
    public void testAddRole() throws Exception {
        MockTestBase.RolesResource service = getService();
        assertNotNull(service);

        Roles roles = service.list(null);
        assertNotNull(roles);
        assertTrue(roles.getRoles().size() > 0);

        Role role = new Role();
        Integer id = roles.getRoles().size() + 1;
        role.setName("role" + id);
        role.setDescription("test role");

        Role result = service.add(role);
        assertNotNull(result);
        checkRole(result);
        assertEquals(id.toString(), result.getId());
        assertEquals(role.getName(), result.getName());
        assertEquals(role.getDescription(), result.getDescription());
    }

    @Test
    public void testRemoveRoles() throws Exception {
        MockTestBase.RolesResource service = getService();
        assertNotNull(service);

        Roles roles = service.list(null);
        assertNotNull(roles);
        assertTrue(roles.getRoles().size() > 0);

        for (Role role : roles.getRoles()) {
            service.remove(role.getId(), new Role());
        }

        roles = service.list(null);
        assertNotNull(roles);
        assertTrue(roles.getRoles().size() == 0);
    }
}

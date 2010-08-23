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

import org.junit.Test;

import java.util.List;

import com.redhat.rhevm.api.model.Role;


public class PowerShellRoleTest extends PowerShellModelTest {

    private static final String ROLE_ID = "00000000-0000-0000-0001-000000000001";
    private static final String ROLE_NAME = "RHEVMUser";
    private static final String ROLE_DESCRCIPTION = "RHEVM user";
    private static final String USER_ID = "cca00b9f-4492-4a82-b71e-082dcdf0fc66";

    @Test
    public void testParseRole() throws Exception {
        String data = readFileContents("role.xml");
        assertNotNull(data);

        List<Role> roles = PowerShellRole.parse(getParser(), data);

        assertEquals(roles.size(), 1);

        verifyRole(roles.get(0), true, false);
    }

    @Test
    public void testParseRoleWithUserId() throws Exception {
        String data = readFileContents("role.xml");
        assertNotNull(data);

        List<Role> roles = PowerShellRole.parse(getParser(), USER_ID, data);

        assertEquals(roles.size(), 1);

        verifyRole(roles.get(0), true, true);
    }

    @Test
    public void testParsePermission() throws Exception {
        String data = readFileContents("permission.xml");
        assertNotNull(data);

        List<Role> roles = PowerShellRole.parse(getParser(), data);

        assertEquals(roles.size(), 1);

        verifyRole(roles.get(0), false, false);
    }

    private void verifyRole(Role r, boolean expectDescription, boolean expectUser) {
        assertEquals(ROLE_ID, r.getId());
        assertEquals(ROLE_NAME, r.getName());
        if (expectDescription) {
            assertEquals(ROLE_DESCRCIPTION, r.getDescription());
        }
        if (expectUser) {
            assertTrue(r.isSetUser());
            assertTrue(r.getUser().isSetId());
            assertEquals(USER_ID, r.getUser().getId());
        } else {
            assertTrue(!r.isSetUser());
        }
    }
}

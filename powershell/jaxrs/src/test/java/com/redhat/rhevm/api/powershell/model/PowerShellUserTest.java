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

import com.redhat.rhevm.api.model.User;


public class PowerShellUserTest extends PowerShellModelTest {

    private static final String UID = "cca00b9f-4492-4a82-b71e-082dcdf0fc66";
    private static final String USER_NAME = "jimbeam@some_domain.local";
    private static final String EMAIL = "jim@beam.net";
    private static final String[] GROUPS = {
        "Group Policy Creator Owners@some_domain.local/Users",
        "Domain Admins@some_domain.local/Users",
        "Enterprise Admins@some_domain.local/Users",
        "Schema Admins@some_domain.local/Users",
        "Administrators@some_domain.local/Builtin" };
    private static final String LAST_NAME = "beam";
    private static final boolean LOGGED_IN = false;

    @Test
    public void testParse() throws Exception {
        String data = readFileContents("user.xml");
        assertNotNull(data);

        List<User> users = PowerShellUser.parse(getParser(), data);

        assertEquals(users.size(), 1);

        verifyUser(users.get(0));
    }

    private void verifyUser(User u) {
        assertEquals(UID, u.getId());
        assertEquals(USER_NAME, u.getUserName());
        assertEquals(EMAIL, u.getEmail());
        assertEquals(LAST_NAME, u.getLastName());
        assertNotNull(u.getGroups());
        assertTrue(!u.getGroups().getGroups().isEmpty());
        for (int i = 0 ; i < GROUPS.length ; i++) {
            assertEquals(GROUPS[i], u.getGroups().getGroups().get(i).getName());
        }
        assertEquals(LOGGED_IN, u.isLoggedIn());
    }
}

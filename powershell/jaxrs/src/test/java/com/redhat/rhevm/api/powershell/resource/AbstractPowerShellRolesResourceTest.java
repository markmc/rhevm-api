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

import com.redhat.rhevm.api.model.Role;
import com.redhat.rhevm.api.model.Roles;
import com.redhat.rhevm.api.model.User;

import com.redhat.rhevm.api.powershell.util.PowerShellCmd;

import org.junit.Test;

import static org.easymock.EasyMock.expect;

import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;


public abstract class AbstractPowerShellRolesResourceTest<A extends AbstractPowerShellResource> extends AbstractPowerShellSimpleResourceTest<Role, A> {

    protected static final String ROLE_NAME = "RHEVMfixer";
    protected static final String ROLE_ID = Integer.toString(ROLE_NAME.hashCode());
    protected static final String ROLE_DESCRIPTION = "RHEVM fix-it-up chappie";

    protected String formatRole(String name) {
        return formatXmlReturn("role",
                               new String[] { name },
                               new String[] { ROLE_DESCRIPTION },
                               new String[] {});
    }

    @Test
    public void testList() throws Exception {
        setUpRoleExpectations(getSelectCommand(), formatRole(ROLE_NAME));
        verifyRoles(listRoles());
    }

    protected void setUpRoleExpectations(String command, String ret) throws Exception {
        mockStatic(PowerShellCmd.class);
        expect(PowerShellCmd.runCommand(setUpPoolExpectations(), command)).andReturn(ret);
        replayAll();
    }

    protected void verifyRoles(Roles roles) {
        assertNotNull(roles);
        assertEquals(1, roles.getRoles().size());
        Role role = roles.getRoles().get(0);
        assertNotNull(role);
        assertEquals(ROLE_ID, role.getId());
        assertEquals(ROLE_NAME, role.getName());
        assertEquals(ROLE_DESCRIPTION, role.getDescription());
        verifyUser(role.getUser());
    }

    protected abstract String getSelectCommand();

    protected abstract Roles listRoles();

    protected abstract void verifyUser(User user);
}

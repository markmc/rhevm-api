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

import java.util.concurrent.Executor;

import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.model.Role;
import com.redhat.rhevm.api.model.User;

import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;

import org.junit.Test;

import static org.easymock.EasyMock.expect;

import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;


public class PowerShellRoleResourceTest extends AbstractPowerShellSimpleResourceTest<Role, PowerShellRoleResource> {

    private static final String ROLE_NAME = "RHEVMfixer";
    private static final String ROLE_ID = Integer.toString(ROLE_NAME.hashCode());
    private static final String USER_ID = "00000000-0000-0000-0001-000000000004";
    private static final String ROLE_DESCRIPTION = "RHEVM fix-it-up chappie";

    protected PowerShellRoleResource getResource(Executor executor, PowerShellPoolMap poolMap, PowerShellParser parser, UriInfoProvider uriProvider) {
        return new PowerShellRoleResource(ROLE_ID, USER_ID, executor, poolMap, parser, uriProvider);
    }

    protected String formatUser(String name) {
        return formatXmlReturn("role",
                               new String[] { name },
                               new String[] { ROLE_DESCRIPTION },
                               new String[] {});
    }

    @Test
    public void testGet() throws Exception {
        setUriInfo(setUpRoleExpectations("get-role -roleid \"" + ROLE_ID + "\"", formatUser(ROLE_NAME)));
        verifyRole(resource.get());
    }

    private UriInfo setUpRoleExpectations(String command, String ret) throws Exception {
        mockStatic(PowerShellCmd.class);
        expect(PowerShellCmd.runCommand(setUpPoolExpectations(), command)).andReturn(ret);
        UriInfo uriInfo = setUpBasicUriExpectations();
        replayAll();
        return uriInfo;
    }

    private void verifyRole(Role role) {
        assertNotNull(role);
        assertEquals(ROLE_ID, role.getId());
        assertEquals(ROLE_NAME, role.getName());
        assertEquals(ROLE_DESCRIPTION, role.getDescription());
        verifyUser(role.getUser());
        verifyLinks(role);
    }

    private void verifyUser(User user) {
        assertNotNull(user);
        assertEquals(USER_ID, user.getId());
    }
}
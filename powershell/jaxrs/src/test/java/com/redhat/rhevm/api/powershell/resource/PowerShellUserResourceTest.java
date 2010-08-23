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

import com.redhat.rhevm.api.model.User;

import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;

import org.junit.Test;

import static org.easymock.EasyMock.expect;

import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static com.redhat.rhevm.api.powershell.resource.PowerShellUsersResourceTest.FORMAT_ARGS;
import static com.redhat.rhevm.api.powershell.resource.PowerShellUsersResourceTest.USER_ID;
import static com.redhat.rhevm.api.powershell.resource.PowerShellUsersResourceTest.USER_NAME;
import static com.redhat.rhevm.api.powershell.resource.PowerShellUsersResourceTest.verifyUser;

public class PowerShellUserResourceTest extends AbstractPowerShellSimpleResourceTest<User, PowerShellUserResource> {

    protected PowerShellUserResource getResource(Executor executor, PowerShellPoolMap poolMap, PowerShellParser parser) {
        return new PowerShellUserResource(USER_ID, executor, poolMap, parser);
    }

    protected String formatUser(String name) {
        return formatXmlReturn("user",
                               new String[] { name },
                               new String[] { "" },
                               FORMAT_ARGS);
    }

    @Test
    public void testGet() throws Exception {
        setUpUserExpectations("get-user -userid \"" + USER_ID + "\"", formatUser(USER_NAME));
        verifyUser(resource.get());
    }

    private void setUpUserExpectations(String command, String ret) throws Exception {
        mockStatic(PowerShellCmd.class);
        expect(PowerShellCmd.runCommand(setUpPoolExpectations(), command)).andReturn(ret);
        replayAll();
    }

    /*
    private void verifyUser(User user) {
        assertNotNull(user);
        assertEquals(USER_ID, user.getId());
        assertEquals(USER_NAME, user.getName());
        assertEquals(USER_NAME + "@" + DOMAIN_NAME, user.getUserName());
        assertEquals(LAST_NAME, user.getLastName());
        assertEquals(EMAIL, user.getEmail());
        assertEquals(DOMAIN_NAME, user.getDomain());
        verifyRoles(user.getLinks());
    }

    private void verifyRoles(List<Link> links) {
        assertNotNull(links);
        assertTrue(!links.isEmpty());
        boolean hasRoles = false;
        for (Link link : links) {
            assertTrue(link.isSetHref());
            if (hasRoles = "roles".equals(link.getRel())) {
                break;
            }
        }
        assertTrue(hasRoles);
    }
    */
}
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
package com.redhat.rhevm.api.powershell.resource;

import java.util.concurrent.Executor;

import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.model.User;
import com.redhat.rhevm.api.model.Users;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;

import org.junit.Test;

import static org.easymock.classextension.EasyMock.expect;

import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;

public class PowerShellDomainUsersResourceTest extends AbstractPowerShellSimpleResourceTest<User, PowerShellDomainUsersResource> {

    private static final String DOMAIN_NAME = "blaa.waterford.com";
    private static final String USER_NAME = "jerry";
    private static final String LAST_NAME = "doolittle";
    private static final String EMAIL = "jerryd@gmail.com";
    private static final String USER_ID = Integer.toString(USER_NAME.hashCode());

    private static final String[] FORMAT_ARGS = { LAST_NAME, EMAIL, DOMAIN_NAME };

    private static final String LIST_COMMAND = "select-user -ad -domain \"" + DOMAIN_NAME + "\" | ? { !$_.isgroup() }";
    private static final String GET_COMMAND = "select-user -ad -domain \"" + DOMAIN_NAME + "\" | ? { $_.userid -eq \"" + USER_ID + "\" }";

    protected PowerShellDomainUsersResource getResource(Executor executor, PowerShellPoolMap poolMap, PowerShellParser parser, UriInfoProvider uriProvider) {
        return new PowerShellDomainUsersResource(new PowerShellDomainResource(new PowerShellDomainsResource(), DOMAIN_NAME),
                                                 executor, poolMap, parser);
    }

    @Test
    public void testList() throws Exception {
        setUpUserExpectations(LIST_COMMAND, formatUser(USER_NAME));
        resource.setUriInfo(setUpBasicUriExpectations());
        replayAll();
        verifyUsers(resource.list());
    }

    @Test
    public void testGet() throws Exception {
        PowerShellDomainUserResource subresource = new PowerShellDomainUserResource(resource, USER_ID);
        setUpUserExpectations(GET_COMMAND, formatUser(USER_NAME));
        resource.setUriInfo(setUpBasicUriExpectations());
        replayAll();
        verifyUser(subresource.get());
    }

    protected void verifyUsers(Users users) {
        assertNotNull(users);
        assertEquals(1, users.getUsers().size());
        verifyUser(users.getUsers().get(0));
    }

    static void verifyUser(User user) {
        assertNotNull(user);
        assertEquals(Integer.toString(USER_NAME.hashCode()), user.getId());
        assertEquals(USER_NAME, user.getName());
        assertEquals(USER_NAME + "@" + DOMAIN_NAME, user.getUserName());
        assertEquals(LAST_NAME, user.getLastName());
        assertEquals(EMAIL, user.getEmail());
        verifyLinks(user);
        assertNotNull(user.getDomain());
        assertEquals(DOMAIN_NAME, user.getDomain().getId());
        verifyLinks(user.getDomain());
    }

    protected String formatUser(String name) {
        return formatXmlReturn("user",
                               new String[] { name },
                               new String[] { "" },
                               FORMAT_ARGS);
    }

    protected void setUpUserExpectations(String command, String ret) throws Exception {
        mockStatic(PowerShellCmd.class);
        expect(PowerShellCmd.runCommand(setUpPoolExpectations(), command)).andReturn(ret);
    }
}

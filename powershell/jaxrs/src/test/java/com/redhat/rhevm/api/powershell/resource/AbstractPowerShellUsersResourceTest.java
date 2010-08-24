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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Link;
import com.redhat.rhevm.api.model.Role;
import com.redhat.rhevm.api.model.User;
import com.redhat.rhevm.api.model.Users;

import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;

import static org.easymock.EasyMock.expect;

import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;


public abstract class AbstractPowerShellUsersResourceTest<A extends AbstractPowerShellResource>  extends AbstractPowerShellSimpleResourceTest<Role, A> {

    static final String FIRST_NAME = "Sylvester";
    static final String LAST_NAME = "McMonkey McBean";
    static final String USER_NAME = FIRST_NAME + " " + LAST_NAME;
    static final String USER_ID = Integer.toString(USER_NAME.hashCode());
    static final String EMAIL = FIRST_NAME + "@fix.it.up.chappie";
    static final String DOMAIN_NAME = "sneetch.beach";
    static final String[] FORMAT_ARGS = { LAST_NAME, EMAIL, DOMAIN_NAME } ;

    protected String formatUser(String name) {
        return formatXmlReturn("user",
                               new String[] { name },
                               new String[] { "" },
                               FORMAT_ARGS);
    }

    protected abstract A getResource(Executor executor, PowerShellPoolMap poolMap, PowerShellParser parser);

    protected void setUpUserExpectations(String[] commands, String[] returns) throws Exception {
        setUpUserExpectations(commands, returns, false);
    }

    protected void setUpUserExpectations(String[] commands, String[] returns, boolean replay) throws Exception {
        mockStatic(PowerShellCmd.class);
        for (int i = 0 ; i < commands.length ; i++) {
            expect(PowerShellCmd.runCommand(setUpPoolExpectations(), commands[i])).andReturn(returns[i]);
        }
        if (replay) {
            replayAll();
        }
    }

    protected UriInfo setUpUriExpectations(QueryParam query) throws Exception {
        return setUpUriExpectations(query, false);
    }

    @SuppressWarnings("unchecked")
    protected UriInfo setUpUriExpectations(QueryParam query, boolean add) throws Exception {
        UriInfo uriInfo = createMock(UriInfo.class);
        if (query != null) {
            MultivaluedMap<String, String> queries = createMock(MultivaluedMap.class);
            List<String> queryParam = new ArrayList<String>();
            queryParam.add(query.value());
            expect(queries.get("search")).andReturn(queryParam).anyTimes();
            expect(uriInfo.getQueryParameters()).andReturn(queries).anyTimes();
        } else {
            expect(uriInfo.getQueryParameters()).andReturn(null).anyTimes();
        }
        if (add) {
            String href = URI_ROOT + SLASH + "users" + SLASH + USER_ID;
            UriBuilder uriBuilder = createMock(UriBuilder.class);
            expect(uriInfo.getAbsolutePathBuilder()).andReturn(uriBuilder);
            expect(uriBuilder.path(USER_ID)).andReturn(uriBuilder);
            expect(uriBuilder.build()).andReturn(new URI(href)).anyTimes();
        }
        replayAll();
        return uriInfo;
    }

    protected void verifyUsers(Users users) {
        assertNotNull(users);
        assertEquals(1, users.getUsers().size());
        verifyUser(users.getUsers().get(0));
    }

    static void verifyUser(User user) {
        assertNotNull(user);
        assertEquals(USER_ID, user.getId());
        assertEquals(USER_NAME, user.getName());
        assertEquals(USER_NAME + "@" + DOMAIN_NAME, user.getUserName());
        assertEquals(LAST_NAME, user.getLastName());
        assertEquals(EMAIL, user.getEmail());
        assertEquals(DOMAIN_NAME, user.getDomain());
        verifyRoles(user.getLinks());
    }

    private static void verifyRoles(List<Link> links) {
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
}

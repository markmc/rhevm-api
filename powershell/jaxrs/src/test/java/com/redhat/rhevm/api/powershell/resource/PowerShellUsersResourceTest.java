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

import java.text.MessageFormat;
import java.util.concurrent.Executor;

import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.model.Role;
import com.redhat.rhevm.api.model.Roles;
import com.redhat.rhevm.api.model.User;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;

import org.junit.Test;

public class PowerShellUsersResourceTest extends AbstractPowerShellUsersResourceTest<PowerShellUsersResource> {

    protected static final String ROLE_ID = "00000000-0000-0000-000000000001";
    protected static final String ROLE_NAME = "RHEVMPowerUser";
    protected static final String ROLE_DESCRIPTION = "star removal power";
    protected static final String SELECT_COMMAND = "select-user -searchtext \"" + QUERY + "\"";
    protected static final String REMOVE_COMMAND = "remove-user -userid \"" + USER_ID + "\"";
    protected static final String GET_USER_BY_NAME = "$u = select-user -AD -searchtext \"" + USER_NAME + "\"; ";
    protected static final String ADD_USER_BY_NAME = "add-user -userid $u.UserId -userroleid {0}";
    protected static final String ADD_USER_BY_ID = "add-user -userid \"" + USER_ID + "\" -userroleid {0}";
    protected static final String GET_ROLE_BY_NAME = "$role = get-roles | ? '{' $_.Name -eq \"{0}\" '}'; ";
    protected static final String ROLE_BY_NAME_ARG = "$role.Id";
    protected static final String ATTACH_ROLE_COMMAND = "attach-role -roleid \"" + ROLE_ID + "\" -elementid " + USER_ID;

    protected PowerShellUsersResource getResource(Executor executor, PowerShellPoolMap poolMap, PowerShellParser parser, UriInfoProvider uriProvider) {
        PowerShellUsersResource resource = new PowerShellUsersResource();
        resource.setExecutor(executor);
        resource.setPowerShellPoolMap(poolMap);
        resource.setParser(parser);
        return resource;
    }

    protected String formatRole(String name) {
        return formatXmlReturn("role",
                               new String[] { name },
                               new String[] { ROLE_DESCRIPTION },
                               new String[] {});
    }

    @Test
    public void testAddByUserIdRoleId() throws Exception {
        User user = getUser(USER_ID, ROLE_ID);
        setUpUserExpectations(asArray(MessageFormat.format(ADD_USER_BY_ID, "\"" + ROLE_ID + "\"")),
                              asArray(formatUser(USER_NAME)));
        resource.setUriInfo(setUpUriExpectations(null, true));
        verifyResponse(resource.add(user), USER_NAME, null, "users");
    }

    @Test
    public void testAddByUserIdMultipleRoleId() throws Exception {
        User user = getUser(USER_ID, ROLE_ID, ROLE_ID);
        setUpUserExpectations(asArrayV(MessageFormat.format(ADD_USER_BY_ID, "\"" + ROLE_ID + "\""),
                                       ATTACH_ROLE_COMMAND),
                              asArrayV(formatUser(USER_NAME), formatRole(ROLE_NAME)));
        resource.setUriInfo(setUpUriExpectations(null, true));
        verifyResponse(resource.add(user), USER_NAME, null, "users");
    }

    @Test
    public void testAddByUserIdRoleName() throws Exception {
        User user = getUser(USER_ID, ROLE_NAME);
        setUpUserExpectations(asArray(MessageFormat.format(GET_ROLE_BY_NAME, ROLE_NAME)
                                      + MessageFormat.format(ADD_USER_BY_ID, ROLE_BY_NAME_ARG)),
                              asArray(formatUser(USER_NAME)));
        resource.setUriInfo(setUpUriExpectations(null, true));
        verifyResponse(resource.add(user), USER_NAME, null, "users");
    }

    @Test
    public void testAddByUserNameRoleId() throws Exception {
        User user = getUser(USER_NAME, ROLE_ID);
        setUpUserExpectations(asArray(GET_USER_BY_NAME
                                      + MessageFormat.format(ADD_USER_BY_NAME, "\"" + ROLE_ID + "\"")),
                              asArray(formatUser(USER_NAME)));
        resource.setUriInfo(setUpUriExpectations(null, true));
        verifyResponse(resource.add(user), USER_NAME, null, "users");
    }

    @Test
    public void testAddByUserNameRoleName() throws Exception {
        User user = getUser(USER_NAME, ROLE_NAME);
        setUpUserExpectations(asArray(GET_USER_BY_NAME
                                      + MessageFormat.format(GET_ROLE_BY_NAME, ROLE_NAME)
                                      + MessageFormat.format(ADD_USER_BY_NAME, ROLE_BY_NAME_ARG)),
                              asArray(formatUser(USER_NAME)));
        resource.setUriInfo(setUpUriExpectations(null, true));
        verifyResponse(resource.add(user), USER_NAME, null, "users");
    }

    @Test
    public void testList() throws Exception {
        setUpUserExpectations(asArray(SELECT_COMMAND), asArray(formatUser(USER_NAME)));
        resource.setUriInfo(setUpUriExpectations(getQueryParam()));
        verifyUsers(resource.list());
    }

    @Test
    public void testRemove() throws Exception {
        setUpUserExpectations(asArray(REMOVE_COMMAND), asArray(formatUser(USER_NAME)), true);
        resource.remove(USER_ID);
    }

    private User getUser(String u, String... roles) {
        User user = new User();
        if (USER_ID.equals(u)) {
            user.setId(u);
        } else {
            user.setUserName(u);
        }
        user.setRoles(new Roles());
        for (String r : roles) {
            Role role = new Role();
            if (ROLE_ID.equals(r)) {
                role.setId(r);
            } else {
                role.setName(r);
            }
            user.getRoles().getRoles().add(role);
        }
        return user;
    }
}


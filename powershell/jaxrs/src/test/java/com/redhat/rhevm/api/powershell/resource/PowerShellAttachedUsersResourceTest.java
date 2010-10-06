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

import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.model.User;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;

import org.junit.Test;

public class PowerShellAttachedUsersResourceTest extends AbstractPowerShellUsersResourceTest<PowerShellAttachedUsersResource> {

    protected static final String PARENT_ID = "12345678-4321-1234-876543210000";
    protected static final String VM_NAME = "withstand";
    protected static final String GET_VM_COMMAND = "get-vm " + PARENT_ID;
    protected static final String SELECT_COMMAND = "select-user -searchtext \"vm.name=" + VM_NAME + "\"";
    protected static final String REMOVE_COMMAND = "remove-user -userid \"" + USER_ID + "\" -vmid " + PARENT_ID;
    protected static final String GET_USER_BY_ID = "$u = get-user \"" + USER_ID + "\"";
    protected static final String GET_USER_BY_NAME = "$u = select-user -searchtext \"" +  USER_NAME + "\"";
    protected static final String ATTACH_USER_COMMAND = "; attach-user -userobject $u -vmid " + PARENT_ID;

    protected String formatVm(String name) {
        return formatXmlReturn("vm",
                               new String[] { name },
                               new String[] { "" },
                               PowerShellVmsResourceTest.extraArgs);
    }

    protected PowerShellAttachedUsersResource getResource(Executor executor, PowerShellPoolMap poolMap, PowerShellParser parser, UriInfoProvider uriProvider) {
        return new PowerShellAttachedUsersResource(PARENT_ID, executor, poolMap, parser, uriProvider);
    }

    @Test
    public void testList() throws Exception {
        setUpUserExpectations(asArrayV(GET_VM_COMMAND, SELECT_COMMAND), asArrayV(formatVm(VM_NAME), formatUser(USER_NAME)));
        setUriInfo(setUpUriExpectations(getQueryParam()));
        verifyUsers(resource.list());
    }

    @Test
    public void testAddByUserId() throws Exception {
        User user = new User();
        user.setId(USER_ID);
        setUpUserExpectations(asArray(GET_USER_BY_ID + ATTACH_USER_COMMAND), asArray(formatUser(USER_NAME)));
        setUriInfo(setUpUriExpectations(null, true));
        verifyResponse(resource.add(user), USER_NAME, null, "users");
    }

    @Test
    public void testAddByUserName() throws Exception {
        User user = new User();
        user.setUserName(USER_NAME);
        setUpUserExpectations(asArray(GET_USER_BY_NAME + ATTACH_USER_COMMAND), asArray(formatUser(USER_NAME)));
        setUriInfo(setUpUriExpectations(null, true));
        verifyResponse(resource.add(user), USER_NAME, null, "users");
    }

    @Test
    public void testRemove() throws Exception {
        setUpUserExpectations(asArray(REMOVE_COMMAND), asArray(formatUser(USER_NAME)), true);
        resource.remove(USER_ID);
    }
}

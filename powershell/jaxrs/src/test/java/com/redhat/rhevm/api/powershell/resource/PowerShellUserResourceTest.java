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
import com.redhat.rhevm.api.model.User;

import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;

import org.junit.Test;

import static org.easymock.EasyMock.expect;

import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static com.redhat.rhevm.api.powershell.resource.AbstractPowerShellUsersResourceTest.FORMAT_ARGS;
import static com.redhat.rhevm.api.powershell.resource.AbstractPowerShellUsersResourceTest.USER_ID;
import static com.redhat.rhevm.api.powershell.resource.AbstractPowerShellUsersResourceTest.USER_NAME;
import static com.redhat.rhevm.api.powershell.resource.AbstractPowerShellUsersResourceTest.verifyUser;

public class PowerShellUserResourceTest extends AbstractPowerShellSimpleResourceTest<User, PowerShellUserResource> {

    protected PowerShellUserResource getResource(Executor executor, PowerShellPoolMap poolMap, PowerShellParser parser, UriInfoProvider uriProvider) {
        return new PowerShellUserResource(USER_ID, executor, poolMap, parser, uriProvider);
    }

    protected String formatUser(String name) {
        return formatXmlReturn("user",
                               new String[] { name },
                               new String[] { "" },
                               FORMAT_ARGS);
    }

    @Test
    public void testGet() throws Exception {
        setUriInfo(setUpUserExpectations("get-user -userid \"" + USER_ID + "\"", formatUser(USER_NAME)));
        verifyUser(resource.get());
    }

    private UriInfo setUpUserExpectations(String command, String ret) throws Exception {
        mockStatic(PowerShellCmd.class);
        expect(PowerShellCmd.runCommand(setUpPoolExpectations(), command)).andReturn(ret);
        UriInfo uriInfo = setUpBasicUriExpectations();
        replayAll();
        return uriInfo;
    }
}
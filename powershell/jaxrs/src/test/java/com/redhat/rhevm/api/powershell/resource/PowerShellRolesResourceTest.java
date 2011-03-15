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
import com.redhat.rhevm.api.model.Roles;
import com.redhat.rhevm.api.model.User;

import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;

public class PowerShellRolesResourceTest extends AbstractPowerShellRolesResourceTest<PowerShellRolesResource> {

    protected static final String SELECT_COMMAND = "get-roles";

    protected PowerShellRolesResource getResource(Executor executor, PowerShellPoolMap poolMap, PowerShellParser parser, UriInfoProvider uriProvider) {
        PowerShellRolesResource resource = new PowerShellRolesResource();
        resource.setExecutor(executor);
        resource.setPowerShellPoolMap(poolMap);
        resource.setParser(parser);
        return resource;
    }

    protected String getSelectCommand() {
        return SELECT_COMMAND;
    }

    protected Roles listRoles() {
        return resource.list();
    }

    protected void verifyUser(User user) {
        assertNull(user);
    }

    protected void cacheUriInfo(UriInfo uriInfo) {
        resource.setUriInfo(uriInfo);
    }
}

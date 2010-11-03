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

import java.util.List;
import java.util.concurrent.Executor;

import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.model.Role;
import com.redhat.rhevm.api.powershell.model.PowerShellRole;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPool;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
import com.redhat.rhevm.api.resource.PermitsResource;
import com.redhat.rhevm.api.resource.RoleResource;


public class PowerShellRoleResource extends UriProviderWrapper implements RoleResource {

    protected String roleId;
    protected String userId;

    public PowerShellRoleResource(String roleId,
                                  String userId,
                                  Executor executor,
                                  PowerShellPoolMap shellPools,
                                  PowerShellParser parser,
                                  UriInfoProvider uriProvider) {
        super(executor, shellPools, parser, uriProvider);
        this.roleId = roleId;
        this.userId = userId;
    }

    public static List<Role> runAndParse(PowerShellPool pool, PowerShellParser parser, String userId, String command) {
        return PowerShellRole.parse(parser, userId, PowerShellCmd.runCommand(pool, command));
    }

    public static Role runAndParseSingle(PowerShellPool pool, PowerShellParser parser, String userId, String command) {
        List<Role> Roles = runAndParse(pool, parser, userId, command);

        return !Roles.isEmpty() ? Roles.get(0) : null;
    }

    public Role runAndParseSingle(String command) {
        return runAndParseSingle(getPool(), getParser(), userId, command);
    }

    @Override
    public Role get() {
        StringBuilder getRole = new StringBuilder();
        getRole.append("get-role -roleid ").append(PowerShellUtils.escape(roleId));

        return LinkHelper.addLinks(getUriInfo(), runAndParseSingle(getRole.toString()));
    }

    @Override
    public PermitsResource getPermitsResource() {
        return null;
    }

}

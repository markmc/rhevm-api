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
import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.model.Role;
import com.redhat.rhevm.api.model.Roles;
import com.redhat.rhevm.api.resource.GlobalRolesResource;
import com.redhat.rhevm.api.resource.RoleResource;

public class PowerShellGlobalRolesResource extends AbstractPowerShellResource implements GlobalRolesResource {

    public List<Role> runAndParse(String command) {
        return PowerShellRoleResource.runAndParse(getPool(), getParser(), null, command);
    }

    public Role runAndParseSingle(String command) {
        return PowerShellRoleResource.runAndParseSingle(getPool(), getParser(), null, command);
    }

    @Override
    public Roles list() {
        Roles ret = new Roles();
        for (Role Role : runAndParse("get-roles")) {
            ret.getRoles().add(LinkHelper.addLinks(Role));
        }
        return ret;
    }

    @Override
    public RoleResource getRoleSubResource(String roleId) {
        return new PowerShellRoleResource(roleId, null, executor, shellPools, parser);
    }
}

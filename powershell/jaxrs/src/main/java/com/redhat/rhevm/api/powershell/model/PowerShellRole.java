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
package com.redhat.rhevm.api.powershell.model;

import java.util.ArrayList;
import java.util.List;

import com.redhat.rhevm.api.model.Role;
import com.redhat.rhevm.api.model.User;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;

public class PowerShellRole {

    private static String ROLE_TYPE = "RhevmCmd.CLIRole";
    private static String PERMISSION_TYPE = "RhevmCmd.CLIPermission";

    public static List<Role> parse(PowerShellParser parser, String output) {
        return parse(parser, null, output);
    }

    public static List<Role> parse(PowerShellParser parser, String userId, String output) {
        List<Role> ret = new ArrayList<Role>();

        for (PowerShellParser.Entity entity : parser.parse(output)) {
            if (ROLE_TYPE.equals(entity.getType())) {
                Role role = new Role();
                role.setId(entity.get("id"));
                role.setName(entity.get("name"));
                role.setDescription(entity.get("description"));
                ret.add(addUser(role, userId));
            } else if (PERMISSION_TYPE.equals(entity.getType())) {
                Role role = new Role();
                role.setId(entity.get("roleid"));
                role.setName(entity.get("rolename"));
                ret.add(addUser(role, userId));
            }
        }

        return ret;
    }

    private static Role addUser(Role role, String userId) {
        if (userId != null) {
            role.setUser(new User());
            role.getUser().setId(userId);
        }
        return role;
    }
}

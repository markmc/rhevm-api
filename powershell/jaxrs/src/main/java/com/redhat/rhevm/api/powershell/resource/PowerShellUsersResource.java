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

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.model.Role;
import com.redhat.rhevm.api.model.User;
import com.redhat.rhevm.api.model.Users;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
import com.redhat.rhevm.api.resource.UserResource;
import com.redhat.rhevm.api.resource.UsersResource;

import static com.redhat.rhevm.api.common.util.CompletenessAssertor.validateParameters;

public class PowerShellUsersResource extends AbstractPowerShellResource implements UsersResource {

    public List<User> runAndParse(String command) {
        return PowerShellUserResource.runAndParse(getPool(), getParser(), command);
    }

    public User runAndParseSingle(String command) {
        return PowerShellUserResource.runAndParseSingle(getPool(), getParser(), command);
    }

    @Override
    public Users list(UriInfo uriInfo) {
        Users ret = new Users();
        for (User user : runAndParse(getSelectCommand("select-user", uriInfo, User.class))) {
            ret.getUsers().add(LinkHelper.addLinks(user));
        }
        return ret;
    }

    @Override
    public Response add(UriInfo uriInfo, User user) {
        validateParameters(user, "userName|id", "roles.id|name");
        StringBuilder buf = new StringBuilder();

        String userArg = null;
        if (user.isSetId()) {
            userArg = PowerShellUtils.escape(user.getId());
        } else {
            buf.append("$u = select-user -AD").append(SEARCH_TEXT);
            buf.append(PowerShellUtils.escape(user.getUserName())).append(";");
            userArg = "$u.UserId";
        }

        String roleArg = getRoleArg(user.getRoles().getRoles().get(0), buf);

        buf.append("add-user -userid ").append(userArg).append(" -userroleid ").append(roleArg);
        User newUser = LinkHelper.addLinks(runAndParseSingle(buf.toString()));
        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(newUser.getId());

        if (user.getRoles().getRoles().size() > 1) {
            for (int i = 1 ; i < user.getRoles().getRoles().size() ; i++) {
                StringBuilder addRole = new StringBuilder();
                String extraRoleArg = getRoleArg(user.getRoles().getRoles().get(i), addRole);
                addRole.append("attach-role -roleid ").append(extraRoleArg)
                       .append(" -elementid ").append(newUser.getId());
                PowerShellRoleResource.runAndParseSingle(getPool(), getParser(), newUser.getId(), addRole.toString());
            }
        }
        return Response.created(uriBuilder.build()).entity(newUser).build();
    }

    @Override
    public void remove(String id) {
        PowerShellCmd.runCommand(getPool(), "remove-user -userid " + PowerShellUtils.escape(id));
    }

    @Override
    public UserResource getUserSubResource(UriInfo uriInfo, String id) {
        return new PowerShellUserResource(id, executor, shellPools, parser);
    }

    static String getRoleArg(Role role, StringBuilder buf) {
        String roleArg = null;
        if (role.isSetId()) {
            roleArg = PowerShellUtils.escape(role.getId());
        } else {
            // no get-roles -searchtext option
            buf.append("$roles = get-roles;");
            buf.append("foreach ($r in $roles) {");
            buf.append("  if ($r.Name -eq ").append(PowerShellUtils.escape(role.getName())).append(") {");
            buf.append("    $role = get-role -roleid $r.Id");
            buf.append("  }");
            buf.append("};");
            roleArg = "$role.Id";
        }
        return roleArg;
    }
}

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
import java.util.List;
import java.util.concurrent.Executor;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.model.User;
import com.redhat.rhevm.api.model.Users;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.resource.UserResource;
import com.redhat.rhevm.api.resource.AttachedUsersResource;

import static com.redhat.rhevm.api.common.util.CompletenessAssertor.validateParameters;
import static com.redhat.rhevm.api.powershell.util.PowerShellUtils.escape;

public class PowerShellAttachedUsersResource extends AbstractPowerShellResource implements AttachedUsersResource {

    private static String PARENT_ID = " -vmid ";
    private static String PARENT_SEARCH = SEARCH_TEXT + "\"vm.name={0}\"";

    private String parentId;

    public PowerShellAttachedUsersResource(String parentId,
                                           Executor executor,
                                           PowerShellPoolMap shellPools,
                                           PowerShellParser parser) {
        super(executor, shellPools, parser);
        this.parentId = parentId;
    }

    public List<User> runAndParse(String command) {
        return PowerShellUserResource.runAndParse(getPool(), getParser(), command);
    }

    public User runAndParseSingle(String command) {
        return PowerShellUserResource.runAndParseSingle(getPool(), getParser(), command);
    }

    @Override
    public Users list(UriInfo uriInfo) {
        Users ret = new Users();
        // apparently needs to be run a separate command, as substituting in $vm.name
        // into the select-user searchtext leads to a powershell syntax error
        StringBuilder getVm = new StringBuilder();
        getVm.append("get-vm ").append(parentId);
        VM vm = PowerShellVmResource.runAndParseSingle(getPool(), getParser(), getVm.toString());

        StringBuilder getUsers = new StringBuilder();
        getUsers.append("select-user").append(MessageFormat.format(PARENT_SEARCH, vm.getName()));

        for (User user : runAndParse(getUsers.toString())) {
            ret.getUsers().add(LinkHelper.addLinks(user));
        }
        return ret;
    }

    @Override
    public Response add(UriInfo uriInfo, User user) {
        validateParameters(user, "userName|id");
        StringBuilder attachUser = new StringBuilder();

        if (user.isSetId()) {
            attachUser.append("$u = get-user ")
                      .append(escape(user.getId())).append("; ");
        } else {
            attachUser.append("$u = select-user").append(SEARCH_TEXT)
                      .append(escape(user.getUserName())).append("; ");
        }

        attachUser.append("attach-user -userobject $u")
                  .append(PARENT_ID).append(parentId);
        User newUser = LinkHelper.addLinks(runAndParseSingle(attachUser.toString()));
        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(newUser.getId());
        return Response.created(uriBuilder.build()).entity(newUser).build();
    }

    @Override
    public void remove(String id) {
        StringBuilder removeUser = new StringBuilder();
        removeUser.append("remove-user -userid ").append(escape(id))
                  .append(PARENT_ID).append(parentId);
        PowerShellCmd.runCommand(getPool(), removeUser.toString());
    }

    @Override
    public UserResource getUserSubResource(UriInfo uriInfo, String id) {
        return new PowerShellUserResource(id, executor, shellPools, parser);
    }
}

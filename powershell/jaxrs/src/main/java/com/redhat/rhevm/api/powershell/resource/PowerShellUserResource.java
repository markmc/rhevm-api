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

import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.model.Link;
import com.redhat.rhevm.api.model.User;
import com.redhat.rhevm.api.powershell.model.PowerShellUser;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPool;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
import com.redhat.rhevm.api.resource.AssignedRolesResource;
import com.redhat.rhevm.api.resource.AssignedTagsResource;
import com.redhat.rhevm.api.resource.UserResource;

public class PowerShellUserResource extends AbstractPowerShellResource implements UserResource {

    protected String id;

    public PowerShellUserResource(String id,
                                  Executor executor,
                                  PowerShellPoolMap shellPools,
                                  PowerShellParser parser) {
        super(executor, shellPools, parser);
        this.id = id;
    }

    public static List<User> runAndParse(PowerShellPool pool, PowerShellParser parser, String command) {
        return PowerShellUser.parse(parser, PowerShellCmd.runCommand(pool, command));
    }

    public static User runAndParseSingle(PowerShellPool pool, PowerShellParser parser, String command) {
        List<User> users = runAndParse(pool, parser, command);
        return !users.isEmpty() ? users.get(0) : null;
    }

    public User runAndParseSingle(String command) {
        return runAndParseSingle(getPool(), getParser(), command);
    }

    public static User addLinks(User user) {
        String [] subCollections = { "roles" };

        user.getLinks().clear();

        for (String collection : subCollections) {
            addSubCollection(user, collection);
        }

        return LinkHelper.addLinks(user);
    }

    @Override
    public User get() {
        StringBuilder getUser = new StringBuilder();
        getUser.append("get-user -userid ").append(PowerShellUtils.escape(id));
        return addLinks(runAndParseSingle(getUser.toString()));
    }

    @Override
    public AssignedRolesResource getRolesResource() {
        return new PowerShellAssignedRolesResource(id, getExecutor(), shellPools, getParser());
    }

    @Override
    public AssignedTagsResource getTagsResource() {
        return null;
    }

    private static void addSubCollection(User user, String collection) {
        Link link = new Link();
        link.setRel(collection);
        link.setHref(LinkHelper.getUriBuilder(user).path(collection).build().toString());
        user.getLinks().add(link);
    }
}

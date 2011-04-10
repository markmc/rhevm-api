/*
 * Copyright © 2011 Red Hat, Inc.
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
import com.redhat.rhevm.api.model.Domain;
import com.redhat.rhevm.api.model.User;
import com.redhat.rhevm.api.model.Users;
import com.redhat.rhevm.api.powershell.model.PowerShellUser;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
import com.redhat.rhevm.api.resource.DomainUserResource;
import com.redhat.rhevm.api.resource.DomainUsersResource;

public class PowerShellDomainUsersResource extends InjectableUriProviderBase implements DomainUsersResource {

    private PowerShellDomainResource parent;

    public PowerShellDomainUsersResource(PowerShellDomainResource parent,
                                         Executor executor,
                                         PowerShellPoolMap shellPools,
                                         PowerShellParser parser) {
        super(executor, shellPools, parser);
        this.parent = parent;
    }

    public String getDomainName() {
        return parent.getId();
    }

    @Override
    public Users list() {
        StringBuilder buf = new StringBuilder();

        buf.append("select-user -ad ");
        buf.append("-domain " + PowerShellUtils.escape(getDomainName()));
        buf.append(" | ? ");
        buf.append("{ !$_.isgroup() }");

        Users ret = new Users();
        for (User user : runAndParse(buf.toString())) {
            ret.getUsers().add(addLinks(user));
        }
        return ret;
    }

    @Override
    public DomainUserResource getDomainUserSubResource(String id) {
        return new PowerShellDomainUserResource(this, id);
    }

    protected List<User> runAndParse(String command) {
        return PowerShellUser.parse(getParser(), PowerShellCmd.runCommand(getPool(), command));
    }

    public User runAndParseSingle(String command) {
        List<User> users = runAndParse(command);
        return !users.isEmpty() ? users.get(0) : null;
    }

    public User addLinks(User user) {
        user.setDomain(new Domain());
        user.getDomain().setId(getDomainName());

        user.getLinks().clear();
        return LinkHelper.addLinks(getUriInfo(), user);
    }
}

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
import com.redhat.rhevm.api.model.Group;
import com.redhat.rhevm.api.model.Groups;
import com.redhat.rhevm.api.powershell.model.PowerShellGroup;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
import com.redhat.rhevm.api.resource.DomainGroupResource;
import com.redhat.rhevm.api.resource.DomainGroupsResource;

public class PowerShellDomainGroupsResource extends InjectableUriProviderBase implements DomainGroupsResource {

    private PowerShellDomainResource parent;

    public PowerShellDomainGroupsResource(PowerShellDomainResource parent,
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
    public Groups list() {
        StringBuilder buf = new StringBuilder();

        buf.append("select-user -ad ");
        buf.append("-domain " + PowerShellUtils.escape(getDomainName()));
        buf.append(" | ? ");
        buf.append("{ $_.isgroup() }");

        Groups ret = new Groups();
        for (Group group : runAndParse(buf.toString())) {
            ret.getGroups().add(addLinks(group));
        }
        return ret;
    }

    @Override
    public DomainGroupResource getDomainGroupSubResource(String id) {
        return new PowerShellDomainGroupResource(this, id);
    }

    protected List<Group> runAndParse(String command) {
        return PowerShellGroup.parse(getParser(), PowerShellCmd.runCommand(getPool(), command));
    }

    public Group runAndParseSingle(String command) {
        List<Group> groups = runAndParse(command);
        return !groups.isEmpty() ? groups.get(0) : null;
    }

    public Group addLinks(Group group) {
        group.setDomain(new Domain());
        group.getDomain().setId(parent.getId());

        group.getLinks().clear();
        return LinkHelper.addLinks(getUriInfo(), group);
    }
}

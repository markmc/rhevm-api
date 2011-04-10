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

import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.model.Domain;
import com.redhat.rhevm.api.model.Domains;
import com.redhat.rhevm.api.powershell.model.PowerShellDomain;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.resource.DomainResource;
import com.redhat.rhevm.api.resource.DomainsResource;

public class PowerShellDomainsResource extends InjectableUriProviderBase implements DomainsResource {

    private String[] SUB_COLLECTIONS = { "groups", "users" };

    @Override
    public Domains list() {
        StringBuilder buf = new StringBuilder();

        buf.append("foreach ($u in select-user) { ");
        buf.append( "$u.domain ");
        buf.append("}");

        Domains ret = new Domains();
        for (Domain domain : runAndParse(buf.toString())) {
            ret.getDomains().add(addLinks(domain));
        }
        return ret;
    }

    public DomainResource getDomainSubResource(String id) {
        return new PowerShellDomainResource(this, id);
    }

    private List<Domain> runAndParse(String command) {
        return PowerShellDomain.parse(getParser(), PowerShellCmd.runCommand(getPool(), command));
    }

    public Domain addLinks(Domain domain) {
        domain = LinkHelper.addLinks(getUriInfo(), domain);

        domain.getLinks().clear();
        for (String collection : SUB_COLLECTIONS) {
            LinkHelper.addLink(domain, collection, LinkHelper.LinkFlags.NONE);
        }

        return domain;
    }
}

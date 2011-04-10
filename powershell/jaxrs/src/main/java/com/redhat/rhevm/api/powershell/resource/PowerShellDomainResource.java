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

import com.redhat.rhevm.api.model.Domain;
import com.redhat.rhevm.api.resource.DomainGroupsResource;
import com.redhat.rhevm.api.resource.DomainResource;
import com.redhat.rhevm.api.resource.DomainUsersResource;

public class PowerShellDomainResource implements DomainResource {

    private PowerShellDomainsResource parent;
    private String id;

    public PowerShellDomainResource(PowerShellDomainsResource parent, String id) {
        this.parent = parent;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Domain get() {
        Domain domain = new Domain();
        domain.setId(id);
        domain.setName(id);
        return parent.addLinks(domain);
    }

    public DomainUsersResource getDomainUsersResource() {
        PowerShellDomainUsersResource resource =
            new PowerShellDomainUsersResource(this, parent.getExecutor(), parent.getPowerShellPoolMap(), parent.getParser());
        resource.setUriInfo(parent.getUriInfo());
        return resource;
    }

    public DomainGroupsResource getDomainGroupsResource() {
        PowerShellDomainGroupsResource resource =
            new PowerShellDomainGroupsResource(this, parent.getExecutor(), parent.getPowerShellPoolMap(), parent.getParser());
        resource.setUriInfo(parent.getUriInfo());
        return resource;
    }
}

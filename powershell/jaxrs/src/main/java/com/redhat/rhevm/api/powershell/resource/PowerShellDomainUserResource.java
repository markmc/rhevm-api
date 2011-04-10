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

import com.redhat.rhevm.api.model.User;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
import com.redhat.rhevm.api.resource.DomainUserResource;

public class PowerShellDomainUserResource implements DomainUserResource {

    private PowerShellDomainUsersResource parent;
    private String id;

    public PowerShellDomainUserResource(PowerShellDomainUsersResource parent, String id) {
        this.parent = parent;
        this.id = id;
    }

    public User get() {
        StringBuilder buf = new StringBuilder();

        buf.append("select-user -ad ");
        buf.append("-domain " + PowerShellUtils.escape(parent.getDomainName()));
        buf.append(" | ? ");
        buf.append("{ $_.userid -eq " + PowerShellUtils.escape(id) + " }");

        return parent.addLinks(parent.runAndParseSingle(buf.toString()));
    }
}

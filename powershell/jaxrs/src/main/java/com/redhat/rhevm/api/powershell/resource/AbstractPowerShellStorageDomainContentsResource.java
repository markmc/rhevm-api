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

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.model.BaseResource;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;

public abstract class AbstractPowerShellStorageDomainContentsResource<R extends BaseResource>
    extends AbstractPowerShellResource
    implements UriInfoProvider {

    private PowerShellStorageDomainResource parent;
    private UriInfo ui;

    public AbstractPowerShellStorageDomainContentsResource(PowerShellStorageDomainResource parent,
                                                           PowerShellPoolMap shellPools,
                                                           PowerShellParser parser) {
        super(shellPools, parser);
        this.parent = parent;
    }

    public UriInfo getUriInfo() {
        return ui;
    }

    @Context
    public void setUriInfo(UriInfo uriInfo) {
        ui = uriInfo;
    }

    public String getStorageDomainId() {
        return parent.getId();
    }

    public abstract R runAndParseSingle(String command);
    public abstract R addLinks(UriInfo uriInfo, R model);
}

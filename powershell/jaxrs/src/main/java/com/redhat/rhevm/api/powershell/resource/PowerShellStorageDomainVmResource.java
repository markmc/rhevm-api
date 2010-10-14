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

import java.util.concurrent.Executor;

import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
import com.redhat.rhevm.api.resource.StorageDomainContentResource;

public class PowerShellStorageDomainVmResource
    extends AbstractPowerShellActionableResource<VM>
    implements StorageDomainContentResource<VM> {

    private PowerShellStorageDomainVmsResource parent;

    public PowerShellStorageDomainVmResource(PowerShellStorageDomainVmsResource parent,
                                             String vmId,
                                             Executor executor,
                                             UriInfoProvider uriProvider,
                                             PowerShellPoolMap shellPools,
                                             PowerShellParser parser) {
        super(vmId, executor, uriProvider, shellPools, parser);
        this.parent = parent;
    }

    public String getStorageDomainId() {
        return parent.getStorageDomainId();
    }

    public String getDataCenterId() {
        return parent.getDataCenterId();
    }

    @Override
    public VM get() {
        StringBuilder buf = new StringBuilder();

        buf.append("$sd = get-storagedomain " + PowerShellUtils.escape(getStorageDomainId()));
        buf.append("; ");
        buf.append("if ($sd.domaintype -eq \"Data\") { ");
        buf.append("get-vm");
        buf.append(" -vmid " + PowerShellUtils.escape(getId()));
        buf.append(" } elseif ($sd.domaintype -eq \"Export\") { ");
        buf.append("get-vmimportcandidates");
        buf.append(" -showall");
        buf.append(" -datacenterid " + PowerShellUtils.escape(getDataCenterId()));
        buf.append(" -storagedomainid " + PowerShellUtils.escape(getStorageDomainId()));
        buf.append(" | ? { $_.vmid -eq " + PowerShellUtils.escape(getId()) + " }");
        buf.append(" }");

        return parent.addLinks(getUriInfo(), parent.runAndParseSingle(buf.toString()));
    }
}

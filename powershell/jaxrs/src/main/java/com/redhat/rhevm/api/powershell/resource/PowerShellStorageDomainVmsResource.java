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

import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.common.util.JAXBHelper;
import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.model.VMs;
import com.redhat.rhevm.api.powershell.model.PowerShellVM;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
import com.redhat.rhevm.api.resource.StorageDomainContentResource;
import com.redhat.rhevm.api.resource.StorageDomainContentsResource;

public class PowerShellStorageDomainVmsResource
    extends AbstractPowerShellStorageDomainContentsResource<VM>
    implements StorageDomainContentsResource<VMs, VM> {

    public PowerShellStorageDomainVmsResource(PowerShellStorageDomainResource parent,
                                              PowerShellPoolMap shellPools,
                                              PowerShellParser parser) {
        super(parent, shellPools, parser);
    }

    public List<PowerShellVM> runAndParse(String command) {
        return PowerShellVM.parse(getParser(), PowerShellCmd.runCommand(getPool(), command), null);
    }

    @Override
    public VM runAndParseSingle(String command) {
        List<PowerShellVM> vms = runAndParse(command);

        return !vms.isEmpty() ? vms.get(0) : null;
    }

    @Override
    public VM addLinks(UriInfo uriInfo, VM vm) {
        vm = JAXBHelper.clone("vm", VM.class, vm);

        vm.setStorageDomain(new StorageDomain());
        vm.getStorageDomain().setId(getStorageDomainId());

        return LinkHelper.addLinks(uriInfo, vm);
    }

    @Override
    public VMs list() {
        StringBuilder buf = new StringBuilder();

        buf.append("$sd = get-storagedomain " + PowerShellUtils.escape(getStorageDomainId()));
        buf.append("; ");
        buf.append("if ($sd.domaintype.StartsWith(\"Data\")) { ");
        buf.append("get-vm");
        buf.append(" -storagedomainid " + PowerShellUtils.escape(getStorageDomainId()));
        buf.append(" } elseif ($sd.domaintype -eq \"Export\") { ");
        buf.append("get-vmimportcandidates");
        buf.append(" -showall");
        // buf.append(" -datacenterid " + PowerShellUtils.escape(getDataCenterId()));
        buf.append(" -storagedomainid " + PowerShellUtils.escape(getStorageDomainId()));
        buf.append(" }");

        VMs ret = new VMs();
        for (PowerShellVM vm : runAndParse(buf.toString())) {
            ret.getVMs().add(addLinks(getUriInfo(), vm));
        }
        return ret;
    }

    public StorageDomainContentResource<VM> getStorageDomainContentSubResource(String id) {
        return new PowerShellStorageDomainVmResource(this, id, executor, this, shellPools, getParser());
    }
}

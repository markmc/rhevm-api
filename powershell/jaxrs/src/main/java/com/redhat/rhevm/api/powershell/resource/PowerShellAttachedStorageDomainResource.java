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

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.DataCenter;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.resource.AttachedStorageDomainResource;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;


public class PowerShellAttachedStorageDomainResource
    extends AbstractPowerShellActionableResource<StorageDomain>
    implements AttachedStorageDomainResource {

    private PowerShellAttachedStorageDomainsResource parent;

    PowerShellAttachedStorageDomainResource(PowerShellAttachedStorageDomainsResource parent,
                                            String storageDomainId,
                                            Executor executor,
                                            UriInfoProvider uriProvider,
                                            PowerShellPoolMap shellPools,
                                            PowerShellParser parser) {
        super(storageDomainId, executor, uriProvider, shellPools, parser);
        this.parent = parent;
    }

    public String getDataCenterId() {
        return parent.getDataCenterId();
    }

    protected StorageDomain addLinks(UriInfo uriInfo, StorageDomain storageDomain) {
        return parent.addLinks(uriInfo, storageDomain);
    }

    @Override
    protected StorageDomain getModel() {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(getId());
        storageDomain.setDataCenter(new DataCenter());
        storageDomain.getDataCenter().setId(getDataCenterId());
        return storageDomain;
    }

    @Override
    public StorageDomain get() {
        StringBuilder buf = new StringBuilder();

        buf.append("get-storagedomain");
        buf.append(" -datacenterid " + PowerShellUtils.escape(getDataCenterId()));
        buf.append(" -storagedomainid " + PowerShellUtils.escape(getId()));

        return addLinks(getUriInfo(),
                        PowerShellStorageDomainResource.runAndParseSingle(getPool(), getParser(), buf.toString()));
    }

    @Override
    public Response activate(Action action) {
        return doAction(getUriInfo(), new AttachedStorageDomainActionTask(action, "activate-storagedomain"));
    }

    @Override
    public Response deactivate(Action action) {
        return doAction(getUriInfo(), new AttachedStorageDomainActionTask(action, "deactivate-storagedomain"));
    }

    private class AttachedStorageDomainActionTask extends AbstractPowerShellActionTask {
        public AttachedStorageDomainActionTask(Action action, String command) {
            super(action, command);
        }
        public void execute() {
            StringBuilder buf = new StringBuilder();

            buf.append(command);
            buf.append(" -datacenterid " + PowerShellUtils.escape(getDataCenterId()));
            buf.append(" -storagedomainid " + PowerShellUtils.escape(getId()));

            PowerShellCmd.runCommand(getPool(), buf.toString());
        }
    }
}

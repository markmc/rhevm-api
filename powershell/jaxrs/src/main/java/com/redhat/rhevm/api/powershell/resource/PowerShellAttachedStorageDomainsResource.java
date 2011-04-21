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

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.DataCenter;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.StorageDomains;
import com.redhat.rhevm.api.resource.AttachedStorageDomainResource;
import com.redhat.rhevm.api.resource.AttachedStorageDomainsResource;
import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.common.util.JAXBHelper;
import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.powershell.util.PowerShellException;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;

import static com.redhat.rhevm.api.common.util.CompletenessAssertor.validateParameters;

public class PowerShellAttachedStorageDomainsResource
    extends AbstractPowerShellResource
    implements AttachedStorageDomainsResource, UriInfoProvider {

    private String dataCenterId;
    private UriInfo ui;

    public PowerShellAttachedStorageDomainsResource(String dataCenterId,
                                                    PowerShellPoolMap shellPools,
                                                    PowerShellParser parser) {
        super(shellPools, parser);
        this.dataCenterId = dataCenterId;
    }

    public UriInfo getUriInfo() {
        return ui;
    }

    @Context
    public void setUriInfo(UriInfo uriInfo) {
        ui = uriInfo;
    }

    public String getDataCenterId() {
        return dataCenterId;
    }

    public StorageDomain addLinks(UriInfo uriInfo, StorageDomain storageDomain) {
        storageDomain = JAXBHelper.clone("storage_domain", StorageDomain.class, storageDomain);

        storageDomain.setDataCenter(new DataCenter());
        storageDomain.getDataCenter().setId(dataCenterId);

        return LinkHelper.addLinks(uriInfo, storageDomain);
    }

    @Override
    public StorageDomains list() {
        StorageDomains ret = new StorageDomains();

        StringBuilder buf = new StringBuilder();

        buf.append("get-storagedomain -datacenterid " + PowerShellUtils.escape(dataCenterId));

        List<StorageDomain> storageDomains;
        try {
            storageDomains = PowerShellStorageDomainResource.runAndParse(getPool(), getParser(), buf.toString());
        } catch (PowerShellException e) {
            // Ignore 'There is no Storage Domains in DataCenter' error
            // i.e. there are no storage domains attached to the data center
            return ret;
        }

        for (StorageDomain storageDomain : storageDomains) {
            ret.getStorageDomains().add(addLinks(getUriInfo(), storageDomain));
        }

        return ret;
    }

    @Override
    public Response add(StorageDomain storageDomain) {
        validateParameters(storageDomain, "id|name");

        StringBuilder buf = new StringBuilder();

        String storageDomainArg;
        if (storageDomain.isSetId()) {
            storageDomainArg = PowerShellUtils.escape(storageDomain.getId());
        } else {
            buf.append("$sd = select-storagedomain -searchtext ");
            buf.append(PowerShellUtils.escape("name=" + storageDomain.getName()));
            buf.append(";");
            storageDomainArg = "$sd.storagedomainid";
        }

        buf.append("attach-storagedomain");
        buf.append(" -datacenterid " + PowerShellUtils.escape(dataCenterId));
        buf.append(" -storagedomainid " + storageDomainArg);

        storageDomain = PowerShellStorageDomainResource.runAndParseSingle(getPool(),
                                                                          getParser(),
                                                                          buf.toString());

        UriBuilder uriBuilder = getUriInfo().getAbsolutePathBuilder().path(storageDomain.getId());

        return Response.created(uriBuilder.build()).entity(addLinks(getUriInfo(), storageDomain)).build();
    }

    @Override
    public void remove(String id) {
        StringBuilder buf = new StringBuilder();

        buf.append("detach-storagedomain");
        buf.append(" -datacenterid " + PowerShellUtils.escape(dataCenterId));
        buf.append(" -storagedomainid " + PowerShellUtils.escape(id));

        PowerShellCmd.runCommand(getPool(), buf.toString());
    }

    @Override
    public AttachedStorageDomainResource getAttachedStorageDomainSubResource(String id) {
        return new PowerShellAttachedStorageDomainResource(this, id, executor, this, shellPools, getParser());
    }
}

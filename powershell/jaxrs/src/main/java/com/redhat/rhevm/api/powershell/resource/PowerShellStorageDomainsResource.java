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

import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.LogicalUnit;
import com.redhat.rhevm.api.model.Storage;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.StorageDomains;
import com.redhat.rhevm.api.model.StorageType;
import com.redhat.rhevm.api.resource.StorageDomainResource;
import com.redhat.rhevm.api.resource.StorageDomainsResource;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;

import static com.redhat.rhevm.api.common.util.CompletenessAssertor.validateParameters;


public class PowerShellStorageDomainsResource extends AbstractPowerShellCollectionResource<StorageDomain, PowerShellStorageDomainResource> implements StorageDomainsResource {

    /* Storage domains that have been torn down but not deleted
     * FIXME: this map shouldn't be static and needs synchronization
     */
    private static HashMap<String, PowerShellStorageDomainResource> tornDownDomains =
        new HashMap<String, PowerShellStorageDomainResource>();

    public List<StorageDomain> runAndParse(String command, boolean sharedStatus) {
        return PowerShellStorageDomainResource.runAndParse(getPool(), getParser(), command, sharedStatus);
    }

    public StorageDomain runAndParseSingle(String command, boolean sharedStatus) {
        return PowerShellStorageDomainResource.runAndParseSingle(getPool(), getParser(), command, sharedStatus);
    }

    @Override
    public StorageDomains list(UriInfo uriInfo) {
        StorageDomains ret = new StorageDomains();

        List<StorageDomain> storageDomains = runAndParse("select-storagedomain", true);

        for (StorageDomain storageDomain : storageDomains) {
            ret.getStorageDomains().add(PowerShellStorageDomainResource.addLinks(storageDomain));
        }

        for (String id : tornDownDomains.keySet()) {
            PowerShellStorageDomainResource resource = tornDownDomains.get(id);
            ret.getStorageDomains().add(PowerShellStorageDomainResource.addLinks(resource.getTornDown()));
        }

        return ret;
    }

    @Override
    public Response add(UriInfo uriInfo, StorageDomain storageDomain) {
        validateParameters(storageDomain, "name", "host.id|name", "type", "storage.type");

        Storage storage = storageDomain.getStorage();

        switch (storage.getType()) {
        case NFS:
            validateParameters(storage, "address", "path");
            break;
        case ISCSI:
        case FCP:
            // REVISIT: validate that a logical unit or volume group supplied
            for (LogicalUnit lu : storage.getLogicalUnits()) {
                validateParameters(lu, "id");
            }
            if (storage.isSetVolumeGroup()) {
                validateParameters(storage.getVolumeGroup(), "id");
            }
            break;
        default:
            assert false : storage.getType();
            break;
        }

        StringBuilder buf = new StringBuilder();

        String hostArg = null;
        if (storageDomain.getHost().isSetId()) {
            hostArg = PowerShellUtils.escape(storageDomain.getHost().getId());
        } else {
            buf.append("$h = select-host -searchtext ");
            buf.append(PowerShellUtils.escape("name=" +  storageDomain.getHost().getName()));
            buf.append(";");
            hostArg = "$h.hostid";
        }

        if (storage.getType() == StorageType.ISCSI ||
            storage.getType() == StorageType.FCP &&
            storage.isSetLogicalUnits()) {
            buf.append("$lunids = new-object System.Collections.ArrayList;");

            for (LogicalUnit lu : storage.getLogicalUnits()) {
                if (storage.getType() == StorageType.ISCSI && lu.isSetAddress() && lu.isSetTarget()) {
                    // REVIST: port no., username, password
                    buf.append("$cnx = new-storageserverconnection");
                    buf.append(" -storagetype ISCSI");
                    buf.append(" -connection " + PowerShellUtils.escape(lu.getAddress()));
                    buf.append(" -iqn " + PowerShellUtils.escape(lu.getTarget()));
                    buf.append(";");

                    buf.append("$cnx = connect-storagetohost");
                    buf.append(" -hostid " + hostArg);
                    buf.append(" -storageserverconnectionobject $cnx");
                    buf.append(";");
                }

                buf.append("$lunids.add(" + PowerShellUtils.escape(lu.getId()) + ") | out-null;");
            }
        }

        // REVIST: add support for logging in to ISCSI targets specified for a volume group

        buf.append("add-storagedomain");

        buf.append(" -name " + PowerShellUtils.escape(storageDomain.getName()));

        buf.append(" -hostid " + hostArg);

        buf.append(" -domaintype ");
        switch (storageDomain.getType()) {
        case DATA:
            buf.append("Data");
            break;
        case ISO:
            buf.append("ISO");
            break;
        case EXPORT:
            buf.append("Export");
            break;
        default:
            assert false : storageDomain.getType();
            break;
        }

        buf.append(" -storagetype " + storage.getType().toString());

        switch (storage.getType()) {
        case NFS:
            buf.append(" -storage ");
            buf.append(PowerShellUtils.escape(storage.getAddress() + ":" + storage.getPath()));
            break;
        case ISCSI:
        case FCP:
            if (storage.isSetLogicalUnits()) {
                buf.append(" -lunidlist $lunids");
            } else {
                buf.append(" -volumegroup " + PowerShellUtils.escape(storage.getVolumeGroup().getId()));
            }
            break;
        default:
            break;
        }

        storageDomain = runAndParseSingle(buf.toString(), true);

        storageDomain = PowerShellStorageDomainResource.addLinks(storageDomain);

        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(storageDomain.getId());

        return Response.created(uriBuilder.build()).entity(storageDomain).build();
    }

    @Override
    public void remove(String id) {
        tornDownDomains.remove(id);
        removeSubResource(id);
    }

    @Override
    public StorageDomainResource getStorageDomainSubResource(UriInfo uriInfo, String id) {
        if (tornDownDomains.containsKey(id)) {
            return tornDownDomains.get(id);
        } else {
            return getSubResource(id);
        }
    }

    protected PowerShellStorageDomainResource createSubResource(String id) {
        return new PowerShellStorageDomainResource(id, this, shellPools, getParser());
    }

    /**
     * Add a storage domain from the set of torn down storage domains.
     * <p>
     * This method should be called when a storage domain is torn down. At
     * this point it no longer exists in RHEV-M itself and just needs to
     * be removed using DELETE.
     *
     * @param id the RHEV-M ID of the StorageDomain
     * @param resource the PowerShellStorageDomainResource
     */
    public void addToTornDownDomains(String id, PowerShellStorageDomainResource resource) {
        tornDownDomains.put(id, resource);
    }

    /**
     * Build an absolute URI for a given storage domain
     *
     * @param baseUriBuilder a UriBuilder representing the base URI
     * @param id the storage domain ID
     * @return an absolute URI
     */
    public static String getHref(UriBuilder baseUriBuilder, String id) {
        return baseUriBuilder.clone().path("storagedomains").path(id).build().toString();
    }
}

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
import java.util.UUID;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Storage;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.StorageDomains;
import com.redhat.rhevm.api.model.StorageDomainStatus;
import com.redhat.rhevm.api.resource.StorageDomainResource;
import com.redhat.rhevm.api.resource.StorageDomainsResource;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;

public class PowerShellStorageDomainsResource implements StorageDomainsResource {

    /* FIXME: these maps shouldn't be static and they need synchronization */

    /* Storage domains that have been added and not yet initialized
     * or that have been torn down but not deleted
     */
    private static HashMap<String, PowerShellStorageDomainResource> stagedDomains =
        new HashMap<String, PowerShellStorageDomainResource>();

    /* When we initially create a storage domain resource, we allocate
     * a UUID for it. However, RHEV-M allocates its own UUID when we
     * add the domain for real. We maintain a mapping from/to our own
     * notion of the ID to/from RHEV-M's notion.
     */
    private static HashMap<String, String> toRhevmIdMapping = new HashMap<String, String>();
    private static HashMap<String, String> fromRhevmIdMapping = new HashMap<String, String>();

    @Override
    public StorageDomains list(UriInfo uriInfo) {
        StorageDomains ret = new StorageDomains();

        for (StorageDomain storageDomain : PowerShellStorageDomainResource.runAndParse("select-storagedomain")) {
            if (fromRhevmIdMapping.containsKey(storageDomain.getId())) {
                storageDomain.setId(fromRhevmIdMapping.get(storageDomain.getId()));
            }
            PowerShellStorageDomainResource resource = new PowerShellStorageDomainResource(storageDomain, this);
            UriBuilder uriBuilder = uriInfo.getRequestUriBuilder().path(storageDomain.getId());
            ret.getStorageDomains().add(resource.addLinks(uriBuilder));
        }

        for (String id : stagedDomains.keySet()) {
            PowerShellStorageDomainResource resource = stagedDomains.get(id);
            UriBuilder uriBuilder = uriInfo.getRequestUriBuilder().path(id);
            ret.getStorageDomains().add(resource.addLinks(uriBuilder));
        }

        return ret;
    }

    @Override
    public Response add(UriInfo uriInfo, StorageDomain storageDomain) {
        storageDomain.setId(UUID.randomUUID().toString());
        storageDomain.setStatus(StorageDomainStatus.UNINITIALIZED);

        PowerShellStorageDomainResource resource = new PowerShellStorageDomainResource(storageDomain, this, true);

        stagedDomains.put(storageDomain.getId(), resource);

        UriBuilder uriBuilder = uriInfo.getRequestUriBuilder().path(storageDomain.getId());

        return Response.created(uriBuilder.build()).entity(resource.addLinks(uriBuilder)).build();
    }

    @Override
    public void remove(String id) {
        removeIdMapping(id);
        stagedDomains.remove(id);
    }

    @Override
    public StorageDomainResource getStorageDomainSubResource(UriInfo uriInfo, String id) {
        if (stagedDomains.containsKey(id)) {
            return stagedDomains.get(id);
        } else {
            if (toRhevmIdMapping.containsKey(id)) {
                id = toRhevmIdMapping.get(id);
            }
            return new PowerShellStorageDomainResource(id, this);
        }
    }

    /**
     * Add a storage domain from the set of staged storage domains.
     * <p>
     * This method should be called when a storage domain is torn down. At
     * this point it no longer exists in RHEV-M itself and just needs to
     * be removed using DELETE or resurrected using the initialize action.
     *
     * @param id the RHEV-M ID of the StorageDomain
     * @param resource the PowerShellStorageDomainResource to stage
     */
    public void stageDomain(String id, PowerShellStorageDomainResource resource) {
        if (fromRhevmIdMapping.containsKey(id)) {
            id = fromRhevmIdMapping.get(id);
            resource.setId(id);
            removeIdMapping(id);
        }
        stagedDomains.put(id, resource);
    }

    /**
     * Remove a stoage domain from the set of staged storage domains.
     * <p>
     * This method should be called when a storage domain has been
     * initialized. At this point it exists in RHEV-M itself and
     * the resource representation will be created directly from
     * querying RHEV-M.
     *
     * @param id the ID of the staged StorageDomain
     */
    public void unstageDomain(String id, String rhevmId) {
        stagedDomains.remove(id);
        addIdMapping(id, rhevmId);
    }

    /**
     * Add a new entry in our bidrectional map between the IDs we
     * allocated and the ones allocated by RHEV-M.
     *
     * @param id our ID
     * @parem rhevmId RHEV-M's ID
     */
    private void addIdMapping(String id, String rhevmId) {
        toRhevmIdMapping.put(id, rhevmId);
        fromRhevmIdMapping.put(rhevmId, id);
    }

    /**
     * Remove an entry in our bidirectional map between the IDs we
     * allocated and the ones allocated by RHEV-M.
     *
     * @param id our ID
     */
    private void removeIdMapping(String id) {
        if (toRhevmIdMapping.containsKey(id)) {
            fromRhevmIdMapping.remove(toRhevmIdMapping.get(id));
            toRhevmIdMapping.remove(id);
        }
    }

    /**
     * Map the StorageDomain's RHEV-M ID to our ID.
     *
     * @param storageDomain the StorageDomain to map
     * @return the mapped StorageDomain
     */
    public StorageDomain mapId(StorageDomain storageDomain) {
        if (fromRhevmIdMapping.containsKey(storageDomain.getId())) {
            storageDomain.setId(fromRhevmIdMapping.get(storageDomain.getId()));
        }
        return storageDomain;
    }
}

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
package com.redhat.rhevm.api.dummy.resource;

import java.util.HashMap;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.StorageDomains;
import com.redhat.rhevm.api.model.StorageDomainStatus;
import com.redhat.rhevm.api.resource.StorageDomainsResource;
import com.redhat.rhevm.api.resource.StorageDomainResource;

public class DummyDataCenterStorageDomainsResource implements StorageDomainsResource {

    private HashMap<String, DummyDataCenterStorageDomainResource> storageDomains = new HashMap<String, DummyDataCenterStorageDomainResource>();

    @Override
    public StorageDomains list(UriInfo uriInfo) {
        StorageDomains ret = new StorageDomains();

        for (String id : storageDomains.keySet()) {
            DummyDataCenterStorageDomainResource resource = storageDomains.get(id);
            UriBuilder uriBuilder = uriInfo.getRequestUriBuilder().path(id);
            ret.getStorageDomains().add(resource.addLinks(uriBuilder));
        }

        return ret;
    }

    @Override
    public Response add(UriInfo uriInfo, StorageDomain storageDomain) {
        String id = storageDomain.getId();
        StorageDomainStatus status = StorageDomainStatus.INACTIVE;

        DummyDataCenterStorageDomainResource resource = new DummyDataCenterStorageDomainResource(id, status);

        // FIXME: need "shared status" logic
        resource.getStorageDomain().jaxb.setStatus(status);

        storageDomains.put(id, resource);

        UriBuilder uriBuilder = uriInfo.getRequestUriBuilder().path(id);

        StorageDomain domain = resource.addLinks(uriBuilder);

        return Response.created(uriBuilder.build()).entity(domain).build();
    }

    @Override
    public void remove(String id) {
        StorageDomainStatus status = StorageDomainStatus.UNATTACHED;

        DummyDataCenterStorageDomainResource resource = storageDomains.get(id);

        // FIXME: need "shared status" logic
        resource.getStorageDomain().jaxb.setStatus(status);

        storageDomains.remove(id);
    }

    @Override
    public StorageDomainResource getStorageDomainSubResource(UriInfo uriInfo, String id) {
        return storageDomains.get(id);
    }
}

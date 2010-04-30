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

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.ActionsBuilder;
import com.redhat.rhevm.api.model.ActionValidator;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.StorageDomainStatus;
import com.redhat.rhevm.api.resource.StorageDomainResource;
import com.redhat.rhevm.api.dummy.model.DummyStorageDomain;

public class DummyDataCenterStorageDomainResource implements StorageDomainResource, ActionValidator {

    private String id;
    private StorageDomainStatus status;

    /**
     * Package-protected ctor, never needs to be instantiated by JAX-RS framework.
     *
     * @param storageDomain  encapsulated StorageDomain
     */
    DummyDataCenterStorageDomainResource(String id, StorageDomainStatus status) {
        this.id = id;
        this.status = status;
    }

    DummyStorageDomain getStorageDomain() {
        DummyStorageDomainResource resource = DummyStorageDomainsResource.getStorageDomain(id);
        return resource.getStorageDomain();
    }

    /*
     * FIXME: this sucks
     */
    private StorageDomain cloneStorageDomain(StorageDomain domain, StorageDomainStatus status) {
        StorageDomain ret = new StorageDomain();
        ret.setId(domain.getId());
        ret.setName(domain.getName());
        ret.setActions(domain.getActions());
        ret.setLink(domain.getLink());
        ret.setType(domain.getType());
        ret.setStorage(domain.getStorage());
        ret.setStatus(status);
        return ret;
    }

    public StorageDomain addLinks(UriBuilder uriBuilder) {
        DummyStorageDomainResource resource = DummyStorageDomainsResource.getStorageDomain(id);
        StorageDomain ret = resource.addLinks(uriBuilder, this);
        return cloneStorageDomain(ret, status);
    }

    @Override
    public StorageDomain get(UriInfo uriInfo) {
        return addLinks(uriInfo.getRequestUriBuilder());
    }

    @Override
    public StorageDomain update(UriInfo uriInfo, StorageDomain storageDomain) {
        DummyStorageDomainResource resource = DummyStorageDomainsResource.getStorageDomain(id);
        this.getStorageDomain().update(storageDomain);
        return addLinks(uriInfo.getRequestUriBuilder());
    }

    @Override
    public void initialize() {
        // FIXME: throw an exception
    }

    @Override
    public void activate() {
        // FIXME: error if not attached
        this.status = StorageDomainStatus.ACTIVE;
    }

    @Override
    public void deactivate() {
        // FIXME: error if not active
        this.status = StorageDomainStatus.INACTIVE;
    }

    public boolean validateAction(String action) {
        switch (this.status) {
        case UNINITIALIZED:
        case UNATTACHED:
            return false;
        case ACTIVE:
            return action.equals("deactivate");
        case INACTIVE:
            return action.equals("activate");
        case LOCKED:
        case MIXED:
        default:
            assert false : this.status;
            return false;
        }
    }
}

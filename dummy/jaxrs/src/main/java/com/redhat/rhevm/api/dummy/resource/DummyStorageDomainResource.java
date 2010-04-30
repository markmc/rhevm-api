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
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.StorageDomainStatus;
import com.redhat.rhevm.api.resource.StorageDomainResource;
import com.redhat.rhevm.api.dummy.model.DummyStorageDomain;

public class DummyStorageDomainResource implements StorageDomainResource {

    private DummyStorageDomain storageDomain;

    /**
     * Package-protected ctor, never needs to be instantiated by JAX-RS framework.
     *
     * @param storageDomain  encapsulated StorageDomain
     */
    DummyStorageDomainResource(DummyStorageDomain storageDomain) {
        this.storageDomain = storageDomain;
    }

    /**
     * Package-level accessor for encapsulated StorageDomain
     *
     * @return  encapsulated storageDomain
     */
    DummyStorageDomain getStorageDomain() {
        return storageDomain;
    }

    public StorageDomain addLinks(UriBuilder uriBuilder) {
        ActionsBuilder actionsBuilder = new ActionsBuilder(uriBuilder, StorageDomainResource.class);
        return storageDomain.getJaxb(uriBuilder, actionsBuilder);
    }

    @Override
    public StorageDomain get(UriInfo uriInfo) {
        return addLinks(uriInfo.getRequestUriBuilder());
    }

    @Override
    public StorageDomain update(UriInfo uriInfo, StorageDomain storageDomain) {
        this.storageDomain.update(storageDomain);
        return addLinks(uriInfo.getRequestUriBuilder());
    }

    @Override
    public void initialize() {
        // FIXME: error if not uninitialized
        this.storageDomain.jaxb.setStatus(StorageDomainStatus.UNATTACHED);
    }

    @Override
    public void activate() {
        // FIXME: error if not attached
        this.storageDomain.jaxb.setStatus(StorageDomainStatus.ACTIVE);
    }

    @Override
    public void deactivate() {
        // FIXME: error if not active
        this.storageDomain.jaxb.setStatus(StorageDomainStatus.INACTIVE);
    }
}

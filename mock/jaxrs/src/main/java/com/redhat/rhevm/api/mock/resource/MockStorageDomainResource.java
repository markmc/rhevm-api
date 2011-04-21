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
package com.redhat.rhevm.api.mock.resource;

import java.util.concurrent.Executor;

import javax.ws.rs.core.UriBuilder;

import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.common.util.JAXBHelper;
import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.model.ActionsBuilder;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.StorageDomainStatus;
import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.model.Templates;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.model.VMs;
import com.redhat.rhevm.api.resource.AssignedPermissionsResource;
import com.redhat.rhevm.api.resource.StorageDomainContentsResource;
import com.redhat.rhevm.api.resource.StorageDomainResource;

public class MockStorageDomainResource extends AbstractMockResource<StorageDomain> implements StorageDomainResource {

    /**
     * Package-protected ctor, never needs to be instantiated by JAX-RS framework.
     *
     * @param storageDomain  encapsulated StorageDomain
     * @param executor       executor used for asynchronous actions
     */
    MockStorageDomainResource(String id, Executor executor,  UriInfoProvider uriProvider) {
        super(id, executor, uriProvider);
        getModel().setStatus(StorageDomainStatus.UNATTACHED);
    }

    // FIXME: this needs to be atomic
    public void updateModel(StorageDomain storageDomain) {
        // update writable fields only
        if (storageDomain.isSetName()) {
            getModel().setName(storageDomain.getName());
        }
        if (storageDomain.isSetDescription()) {
            getModel().setDescription(storageDomain.getDescription());
        }
    }

    public StorageDomain addLinks() {
        StorageDomain storageDomain = JAXBHelper.clone(OBJECT_FACTORY.createStorageDomain(getModel()));

        storageDomain = LinkHelper.addLinks(getUriInfo(), storageDomain);

        UriBuilder uriBuilder = LinkHelper.getUriBuilder(getUriInfo(), storageDomain);

        ActionsBuilder actionsBuilder = new ActionsBuilder(uriBuilder, StorageDomainResource.class);
        storageDomain.setActions(actionsBuilder.build());

        return storageDomain;
    }

    @Override
    public StorageDomain get() {
        return addLinks();
    }

    @Override
    public StorageDomain update(StorageDomain storageDomain) {
        validateUpdate(storageDomain);
        updateModel(storageDomain);
        return addLinks();
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        return null;
    }

    public StorageDomainContentsResource<VMs, VM> getStorageDomainVmsResource() {
        return null;
    }

    public StorageDomainContentsResource<Templates, Template> getStorageDomainTemplatesResource() {
        return null;
    }
}

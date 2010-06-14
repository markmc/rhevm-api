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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.mock.util.SimpleQueryEvaluator;
import com.redhat.rhevm.api.model.Attachment;
import com.redhat.rhevm.api.model.Attachments;
import com.redhat.rhevm.api.model.Storage;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.StorageDomains;
import com.redhat.rhevm.api.model.StorageDomainType;
import com.redhat.rhevm.api.model.StorageType;
import com.redhat.rhevm.api.resource.StorageDomainResource;
import com.redhat.rhevm.api.resource.StorageDomainsResource;

import static com.redhat.rhevm.api.mock.resource.AbstractMockResource.allocateId;


public class MockStorageDomainsResource extends AbstractMockQueryableResource<StorageDomain> implements StorageDomainsResource {

    private static Map<String, MockStorageDomainResource> storageDomains =
        Collections.synchronizedMap(new HashMap<String, MockStorageDomainResource>());

    public MockStorageDomainsResource() {
        super(new SimpleQueryEvaluator<StorageDomain>());
    }

    private void addStorageDomain(StorageDomainType domainType, String name, StorageType storageType, String address, String path) {
        MockStorageDomainResource resource = new MockStorageDomainResource(allocateId(StorageDomain.class), getExecutor());

        resource.getModel().setName(name);
        resource.getModel().setType(domainType);

        Storage storage = new Storage();
        storage.setType(storageType);
        storage.setAddress(address);
        storage.setPath(path);
        resource.getModel().setStorage(storage);

        storageDomains.put(resource.getModel().getId(), resource);
    }

    public void populate() {
        synchronized (storageDomains) {
            if (storageDomains.size() == 0) {
                addStorageDomain(StorageDomainType.DATA, "images_0", StorageType.NFS, "172.31.0.6", "/exports/RHEV/images/0");
                addStorageDomain(StorageDomainType.ISO, "isos_0", StorageType.NFS, "172.31.0.6", "/exports/RHEV/iso/0");
            }
        }
    }

    @Override
    public StorageDomains list(UriInfo uriInfo) {
        StorageDomains ret = new StorageDomains();

        for (MockStorageDomainResource storageDomain : storageDomains.values()) {
            if (filter(storageDomain.getModel(), uriInfo, StorageDomain.class)) {
                ret.getStorageDomains().add(storageDomain.addLinks());
            }
        }

        return ret;
    }

    @Override
    public Response add(UriInfo uriInfo, StorageDomain storageDomain) {
        MockStorageDomainResource resource = new MockStorageDomainResource(allocateId(StorageDomain.class), getExecutor());

        resource.updateModel(storageDomain);

        String id = resource.getId();
        storageDomains.put(id, resource);

        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(id);

        storageDomain = resource.addLinks();

        return Response.created(uriBuilder.build()).entity(storageDomain).build();
    }

    @Override
    public void remove(String id) {
        // FIXME: error if not uninitialized
        storageDomains.remove(id);
    }

    @Override
    public StorageDomainResource getStorageDomainSubResource(UriInfo uriInfo, String id) {
        return storageDomains.get(id);
    }

    /**
     * Build a list of storage domains attached to a data center
     *
     * @param dataCenterId  the ID of the data center
     * @return  an encapsulation of the attachments
     */
    public static Attachments getAttachmentsForDataCenter(String dataCenterId) {
        Attachments attachments = new Attachments();

        for (MockStorageDomainResource storageDomain : storageDomains.values()) {
            Attachment attachment = storageDomain.getAttachment(dataCenterId);
            if (attachment != null) {
                attachments.getAttachments().add(attachment);
            }
        }

        return attachments;
    }
}

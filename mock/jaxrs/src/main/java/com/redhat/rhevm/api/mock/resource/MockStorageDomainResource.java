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

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.common.resource.StorageDomainActionValidator;
import com.redhat.rhevm.api.common.util.JAXBHelper;
import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.ActionsBuilder;
import com.redhat.rhevm.api.model.ActionValidator;
import com.redhat.rhevm.api.model.Link;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.StorageDomainStatus;
import com.redhat.rhevm.api.resource.AttachmentsResource;
import com.redhat.rhevm.api.resource.StorageDomainResource;

public class MockStorageDomainResource extends AbstractMockResource<StorageDomain> implements StorageDomainResource {

    private MockAttachmentsResource attachments;

    /**
     * Package-protected ctor, never needs to be instantiated by JAX-RS framework.
     *
     * @param storageDomain  encapsulated StorageDomain
     * @param executor       executor used for asynchronous actions
     */
    MockStorageDomainResource(String id, Executor executor) {
        super(id, executor);
        getModel().setStatus(StorageDomainStatus.UNINITIALIZED);
        this.attachments = new MockAttachmentsResource(id, executor);
    }

    // FIXME: this needs to be atomic
    public void updateModel(StorageDomain storageDomain) {
        // update writable fields only
        getModel().setName(storageDomain.getName());
    }

    public StorageDomain addLinks(UriBuilder uriBuilder) {
        StorageDomain storageDomain = JAXBHelper.clone(OBJECT_FACTORY.createStorageDomain(getModel()));
        storageDomain.setHref(uriBuilder.build().toString());

        ActionValidator actionValidator = new StorageDomainActionValidator(storageDomain);
        ActionsBuilder actionsBuilder = new ActionsBuilder(uriBuilder, StorageDomainResource.class, actionValidator);
        storageDomain.setActions(actionsBuilder.build());

        Link link = new Link();
        link.setRel("attachments");
        link.setHref(uriBuilder.clone().path("attachments").build().toString());
        storageDomain.getLinks().clear();
        storageDomain.getLinks().add(link);
        return storageDomain;
    }

    @Override
    public StorageDomain get(UriInfo uriInfo) {
        return addLinks(uriInfo.getRequestUriBuilder());
    }

    @Override
    public StorageDomain update(HttpHeaders headers, UriInfo uriInfo, StorageDomain storageDomain) {
        validateUpdate(storageDomain, headers);
        updateModel(storageDomain);
        return addLinks(uriInfo.getRequestUriBuilder());
    }

    @Override
    public Response initialize(UriInfo uriInfo, Action action) {
        // FIXME: error if not uninitialized
        return doAction(uriInfo, new StorageDomainStatusSetter(action, StorageDomainStatus.UNATTACHED));
    }

    @Override
    public Response teardown(UriInfo uriInfo, Action action) {
        // FIXME: error if not unattached
        return doAction(uriInfo, new StorageDomainStatusSetter(action, StorageDomainStatus.UNINITIALIZED));
    }

    public AttachmentsResource getAttachmentsResource() {
        return attachments;
    }

    /**
     * Build a URI for any existing attachment to the given data center
     *
     * @param uriInfo  URI context of the current request
     * @param dataCenterId  the ID of the data center
     * @return  a URI representing the attachment
     */
    public String getAttachmentHref(UriInfo uriInfo, String dataCenterId) {
        return attachments.getAttachmentHref(uriInfo, dataCenterId);
    }

    private class StorageDomainStatusSetter extends AbstractActionTask {
        private StorageDomainStatus status;
        public StorageDomainStatusSetter(Action action, StorageDomainStatus status) {
            super(action);
            this.status = status;
        }
        public void run() {
            MockStorageDomainResource.this.getModel().setStatus(status);
        }
    }
}
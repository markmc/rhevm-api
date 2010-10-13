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

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.common.util.JAXBHelper;
import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.ActionsBuilder;
import com.redhat.rhevm.api.model.Attachment;
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
    MockStorageDomainResource(String id, Executor executor,  UriInfoProvider uriProvider) {
        super(id, executor, uriProvider);
        getModel().setStatus(StorageDomainStatus.UNATTACHED);
        this.attachments = new MockAttachmentsResource(id, executor);
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

        Link link = new Link();
        link.setRel("attachments");
        link.setHref(uriBuilder.clone().path("attachments").build().toString());
        storageDomain.getLinks().clear();
        storageDomain.getLinks().add(link);

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
    public Response teardown(Action action) {
        // FIXME: error if not unattached
        return doAction(getUriInfo(), new StorageDomainStatusSetter(action, StorageDomainStatus.TORNDOWN));
    }

    public AttachmentsResource getAttachmentsResource() {
        return attachments;
    }

    /**
     * Build a attachment representation for any existing attachment to
     * the given data center
     *
     * @param dataCenterId  the ID of the data center
     * @return  a representation of the attachment
     */
    public Attachment getAttachment(String dataCenterId) {
        return attachments.getAttachment(dataCenterId);
    }

    private class StorageDomainStatusSetter extends AbstractActionTask {
        private StorageDomainStatus status;
        public StorageDomainStatusSetter(Action action, StorageDomainStatus status) {
            super(action, "StorageDomain status failed with {0}");
            this.status = status;
        }
        public void execute() {
            if (status.equals(MockStorageDomainResource.this.getModel().getStatus())) {
                throw new IllegalStateException("StorageDomain status already: " + status);
            }
            MockStorageDomainResource.this.getModel().setStatus(status);
        }
    }
}

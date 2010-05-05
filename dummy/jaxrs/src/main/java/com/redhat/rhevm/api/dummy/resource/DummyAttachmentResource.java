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
import com.redhat.rhevm.api.model.Attachment;
import com.redhat.rhevm.api.model.DataCenter;
import com.redhat.rhevm.api.model.Link;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.StorageDomainStatus;
import com.redhat.rhevm.api.resource.AttachmentResource;
import com.redhat.rhevm.api.dummy.model.DummyAttachment;

public class DummyAttachmentResource implements AttachmentResource, ActionValidator {

    private DummyAttachment attachment;

    /**
     * Package-protected ctor, never needs to be instantiated by JAX-RS framework.
     *
     * @param attachment  encapsulated Attachment
     */
    DummyAttachmentResource(DummyAttachment attachment) {
        this.attachment = attachment;
        this.attachment.jaxb.setStatus(StorageDomainStatus.INACTIVE);
    }

    /**
     * Package-level accessor for encapsulated Attachment
     *
     * @return  encapsulated attachment
     */
    DummyAttachment getAttachment() {
        return attachment;
    }

    private void setStorageDomainHref(UriBuilder baseUriBuilder) {
        StorageDomain storageDomain = attachment.jaxb.getStorageDomain();

        String href = DummyStorageDomainsResource.getHref(baseUriBuilder, storageDomain.getId());

        Link link = new Link();
        link.setRel("self");
        link.setHref(href);

        storageDomain.getLinks().clear();
        storageDomain.getLinks().add(link);
    }

    private void setDataCenterHref(UriBuilder baseUriBuilder) {
        DataCenter dataCenter = attachment.jaxb.getDataCenter();

        String href = DummyDataCentersResource.getHref(baseUriBuilder, dataCenter.getId());

        Link link = new Link();
        link.setRel("self");
        link.setHref(href);

        dataCenter.getLinks().clear();
        dataCenter.getLinks().add(link);
    }

    public Attachment addLinks(UriInfo uriInfo, UriBuilder uriBuilder) {
        UriBuilder baseUriBuilder = uriInfo.getBaseUriBuilder();

        setStorageDomainHref(baseUriBuilder);
        setDataCenterHref(baseUriBuilder);

        ActionsBuilder actionsBuilder = new ActionsBuilder(uriBuilder, AttachmentResource.class, this);

        return attachment.getJaxb(uriBuilder, actionsBuilder);
    }

    @Override
    public Attachment get(UriInfo uriInfo) {
        return addLinks(uriInfo, uriInfo.getRequestUriBuilder());
    }

    @Override
    public void activate() {
        // FIXME: error if not attached
        this.attachment.jaxb.getStorageDomain().setStatus(StorageDomainStatus.ACTIVE);
    }

    @Override
    public void deactivate() {
        // FIXME: error if not active
        this.attachment.jaxb.getStorageDomain().setStatus(StorageDomainStatus.INACTIVE);
    }

    public boolean validateAction(String action) {
        Attachment jaxb = attachment.jaxb;

        switch (jaxb.getStatus()) {
        case ACTIVE:
            return action.equals("deactivate");
        case INACTIVE:
            return action.equals("activate");
        case UNINITIALIZED:
        case UNATTACHED:
        case LOCKED:
        case MIXED:
        default:
            assert false : jaxb.getStatus();
            return false;
        }
    }
}

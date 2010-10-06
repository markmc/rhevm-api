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

import java.util.HashMap;
import java.util.concurrent.Executor;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.redhat.rhevm.api.model.Attachment;
import com.redhat.rhevm.api.model.Attachments;
import com.redhat.rhevm.api.model.DataCenter;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.resource.AttachmentResource;
import com.redhat.rhevm.api.resource.AttachmentsResource;
import com.redhat.rhevm.api.common.util.LinkHelper;


public class MockAttachmentsResource extends AbstractMockCollectionResource implements AttachmentsResource {
    // keyed off the data center id
    private static HashMap<String, MockAttachmentResource> attachments = new HashMap<String, MockAttachmentResource>();

    private String storageDomainId;

    public MockAttachmentsResource(String storageDomainId, Executor executor) {
        this.storageDomainId = storageDomainId;
        setExecutor(executor);
    }

    @Override
    public Attachments list() {
        Attachments ret = new Attachments();

        for (String id : attachments.keySet()) {
            MockAttachmentResource attachment = attachments.get(id);
            UriBuilder uriBuilder = getUriInfo().getAbsolutePathBuilder().path(id);
            ret.getAttachments().add(attachment.addLinks(uriBuilder));
        }

        return ret;
    }

    @Override
    public Response add(Attachment attachment) {
        // update writable fields only
        if (attachment.getDataCenter() != null) {
            DataCenter dataCenter = new DataCenter();

            // we're only interested in its id
            dataCenter.setId(attachment.getDataCenter().getId());

            attachment.setDataCenter(dataCenter);
        }

        MockAttachmentResource newAttachment = new MockAttachmentResource(attachment, getExecutor(), this);

        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(storageDomainId);
        newAttachment.getModel().setStorageDomain(storageDomain);

        String id = newAttachment.getModel().getDataCenter().getId();
        attachments.put(id, newAttachment);

        UriBuilder uriBuilder = getUriInfo().getAbsolutePathBuilder().path(id);

        attachment = newAttachment.addLinks(uriBuilder);

        return Response.created(uriBuilder.build()).entity(attachment).build();
    }

    @Override
    public void remove(String id) {
        attachments.remove(id);
    }

    @Override
    public AttachmentResource getAttachmentSubResource(String id) {
        return attachments.get(id);
    }

    /**
     * Build a attachment representation for any existing attachment to
     * the given data center
     *
     * @param dataCenterId  the ID of the data center
     * @return  a representation of the attachment
     */
    public Attachment getAttachment(String dataCenterId) {
        if (attachments.get(dataCenterId) == null) {
            return null;
        }

        Attachment attachment = new Attachment();
        attachment.setId(dataCenterId);
        attachment.setStorageDomain(new StorageDomain());
        attachment.getStorageDomain().setId(storageDomainId);

        return LinkHelper.addLinks(getUriInfo(), attachment);
    }
}

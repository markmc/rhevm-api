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

import com.redhat.rhevm.api.model.Attachment;
import com.redhat.rhevm.api.model.Attachments;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.resource.AttachmentResource;
import com.redhat.rhevm.api.resource.AttachmentsResource;
import com.redhat.rhevm.api.dummy.model.DummyAttachment;

public class DummyAttachmentsResource implements AttachmentsResource {
    /* FIXME: synchronize access to this */
    // keyed off the data center id
    private static HashMap<String, DummyAttachmentResource> attachments = new HashMap<String, DummyAttachmentResource>();

    private String storageDomainId;

    public DummyAttachmentsResource(String storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    @Override
    public Attachments list(UriInfo uriInfo) {
        Attachments ret = new Attachments();

        for (String id : attachments.keySet()) {
            DummyAttachmentResource attachment = attachments.get(id);
            UriBuilder uriBuilder = uriInfo.getRequestUriBuilder().path(id);
            ret.getAttachments().add(attachment.addLinks(uriInfo, uriBuilder));
        }

        return ret;
    }

    @Override
    public Response add(UriInfo uriInfo, Attachment attachment) {
        DummyAttachmentResource newAttachment = new DummyAttachmentResource(new DummyAttachment(attachment));

        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(storageDomainId);
        newAttachment.getAttachment().jaxb.setStorageDomain(storageDomain);

        String id = newAttachment.getAttachment().jaxb.getDataCenter().getId();
        attachments.put(id, newAttachment);

        UriBuilder uriBuilder = uriInfo.getRequestUriBuilder().path(id);

        attachment = newAttachment.addLinks(uriInfo, uriBuilder);

        return Response.created(uriBuilder.build()).entity(attachment).build();
    }

    @Override
    public void remove(String id) {
        attachments.remove(id);
    }

    @Override
    public AttachmentResource getAttachmentSubResource(UriInfo uriInfo, String id) {
        return attachments.get(id);
    }
}

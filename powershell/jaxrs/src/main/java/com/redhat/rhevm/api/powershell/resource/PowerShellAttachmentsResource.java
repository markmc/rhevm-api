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
package com.redhat.rhevm.api.powershell.resource;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Attachment;
import com.redhat.rhevm.api.model.Attachments;
import com.redhat.rhevm.api.model.DataCenter;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.resource.AttachmentResource;
import com.redhat.rhevm.api.resource.AttachmentsResource;
import com.redhat.rhevm.api.powershell.util.PowerShellException;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;


public class PowerShellAttachmentsResource implements AttachmentsResource {

    private String storageDomainId;
    private Executor executor;

    public PowerShellAttachmentsResource(String storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    public static Attachment buildAttachment(DataCenter dataCenter, StorageDomain storageDomain) {
        Attachment attachment = new Attachment();

        attachment.setId(dataCenter.getId());
        attachment.setName(dataCenter.getId());

        attachment.setDataCenter(new DataCenter());
        attachment.getDataCenter().setId(dataCenter.getId());

        attachment.setStorageDomain(new StorageDomain());
        attachment.getStorageDomain().setId(storageDomain.getId());

        attachment.setStatus(storageDomain.getStatus());

        if (storageDomain.isSetMaster()) {
            attachment.setMaster(storageDomain.isMaster());
        }

        return attachment;
    }

    @Override
    public Attachments list(UriInfo uriInfo) {
        Attachments ret = new Attachments();

        StringBuilder buf = new StringBuilder();

        buf.append("get-datacenter -storagedomainid " + storageDomainId);

        ArrayList<DataCenter> dataCenters;
        try {
            dataCenters = PowerShellDataCenterResource.runAndParse(buf.toString());
        } catch (PowerShellException e) {
            // Ignore 'Can not find any DataCenter by StorageDomainId' error
            // i.e. the storage domain isn't attached to any data center
            return ret;
        }

        for (DataCenter dataCenter : dataCenters) {
            buf = new StringBuilder();

            buf.append("get-storagedomain");
            buf.append(" -datacenterid " + dataCenter.getId());
            buf.append(" -storagedomainid " + storageDomainId);

            StorageDomain storageDomain = PowerShellStorageDomainResource.runAndParseSingle(buf.toString());

            Attachment attachment = buildAttachment(dataCenter, storageDomain);

            UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(dataCenter.getId());
            ret.getAttachments().add(PowerShellAttachmentResource.addLinks(attachment, uriInfo, uriBuilder));
        }

        return ret;
    }

    @Override
    public Response add(UriInfo uriInfo, Attachment attachment) {
        DataCenter dataCenter = attachment.getDataCenter();

        StringBuilder buf = new StringBuilder();

        buf.append("attach-storagedomain");
        buf.append(" -datacenterid " + dataCenter.getId());
        buf.append(" -storagedomainid " + storageDomainId);

        StorageDomain storageDomain = PowerShellStorageDomainResource.runAndParseSingle(buf.toString());

        attachment = buildAttachment(dataCenter, storageDomain);

        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(dataCenter.getId());

        attachment = PowerShellAttachmentResource.addLinks(attachment, uriInfo, uriBuilder);

        return Response.created(uriBuilder.build()).entity(attachment).build();
    }

    @Override
    public void remove(String id) {
        StringBuilder buf = new StringBuilder();

        buf.append("detach-storagedomain");
        buf.append(" -datacenterid " + id);
        buf.append(" -storagedomainid " + storageDomainId);

        PowerShellCmd.runCommand(buf.toString());
    }

    @Override
    public AttachmentResource getAttachmentSubResource(UriInfo uriInfo, String id) {
        try {
            StringBuilder buf = new StringBuilder();

            buf.append("get-storagedomain");
            buf.append(" -datacenterid " + id);
            buf.append(" -storagedomainid " + storageDomainId);
            PowerShellCmd.runCommand(buf.toString());

            return new PowerShellAttachmentResource(id, storageDomainId, executor);
        } catch (PowerShellException e) {
            return null;
        }
    }

    /**
     * Build a list of storage domains attached to a data center
     *
     * @param uriInfo  the URI context of the current request
     * @param dataCenterId  the ID of the data center
     * @return  an encapsulation of the attachments
     */
    public static Attachments getAttachmentsForDataCenter(UriInfo uriInfo, String dataCenterId) {
        Attachments attachments = new Attachments();

        StringBuilder buf = new StringBuilder();

        buf.append("get-storagedomain");
        buf.append(" -datacenterid " + dataCenterId);

        ArrayList<StorageDomain> storageDomains;
        try {
            storageDomains = PowerShellStorageDomainResource.runAndParse(buf.toString());
        } catch (PowerShellException e) {
            // Ignore 'There is no Storage Domains in DataCenter' error
            // i.e. no storage domains are attached to this data center
            return attachments;
        }

        for (StorageDomain storageDomain : storageDomains) {
            UriBuilder uriBuilder =
                uriInfo.getBaseUriBuilder().path("storagedomains").path(storageDomain.getId())
                                           .path("attachments").path(dataCenterId);

            Attachment attachment = new Attachment();
            attachment.setHref(uriBuilder.build().toString());

            attachments.getAttachments().add(attachment);

        }

        return attachments;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }
}

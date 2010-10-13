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

import java.util.List;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Attachment;
import com.redhat.rhevm.api.model.Attachments;
import com.redhat.rhevm.api.model.DataCenter;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.resource.AttachmentResource;
import com.redhat.rhevm.api.resource.AttachmentsResource;
import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.powershell.util.PowerShellException;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPool;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;

import static com.redhat.rhevm.api.common.util.CompletenessAssertor.validateParameters;


public class PowerShellAttachmentsResource extends AbstractPowerShellResource implements AttachmentsResource, UriInfoProvider {

    private String storageDomainId;
    private UriInfo ui;

    public PowerShellAttachmentsResource(String storageDomainId,
                                         PowerShellPoolMap shellPools,
                                         PowerShellParser parser) {
        super(shellPools, parser);
        this.storageDomainId = storageDomainId;
    }

    public UriInfo getUriInfo() {
        return ui;
    }

    @Context
    public void setUriInfo(UriInfo uriInfo) {
        ui = uriInfo;
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
    public Attachments list() {
        Attachments ret = new Attachments();

        StringBuilder buf = new StringBuilder();

        buf.append("get-datacenter -storagedomainid " + PowerShellUtils.escape(storageDomainId));

        List<DataCenter> dataCenters;
        try {
            dataCenters = PowerShellDataCenterResource.runAndParse(getPool(), getParser(), buf.toString());
        } catch (PowerShellException e) {
            // Ignore 'Can not find any DataCenter by StorageDomainId' error
            // i.e. the storage domain isn't attached to any data center
            return ret;
        }

        for (DataCenter dataCenter : dataCenters) {
            buf = new StringBuilder();

            buf.append("get-storagedomain");
            buf.append(" -datacenterid " + PowerShellUtils.escape(dataCenter.getId()));
            buf.append(" -storagedomainid " + PowerShellUtils.escape(storageDomainId));

            StorageDomain storageDomain = PowerShellStorageDomainResource.runAndParseSingle(getPool(),
                                                                                            getParser(),
                                                                                            buf.toString());

            Attachment attachment = buildAttachment(dataCenter, storageDomain);

            ret.getAttachments().add(PowerShellAttachmentResource.addLinks(getUriInfo(), attachment));
        }

        return ret;
    }

    @Override
    public Response add(Attachment attachment) {
        validateParameters(attachment, "dataCenter.id");
        DataCenter dataCenter = attachment.getDataCenter();

        StringBuilder buf = new StringBuilder();

        buf.append("attach-storagedomain");
        buf.append(" -datacenterid " + PowerShellUtils.escape(dataCenter.getId()));
        buf.append(" -storagedomainid " + PowerShellUtils.escape(storageDomainId));

        StorageDomain storageDomain = PowerShellStorageDomainResource.runAndParseSingle(getPool(),
                                                                                        getParser(),
                                                                                        buf.toString());

        attachment = buildAttachment(dataCenter, storageDomain);
        attachment = PowerShellAttachmentResource.addLinks(getUriInfo(), attachment);

        UriBuilder uriBuilder = getUriInfo().getAbsolutePathBuilder().path(dataCenter.getId());

        return Response.created(uriBuilder.build()).entity(attachment).build();
    }

    @Override
    public void remove(String id) {
        StringBuilder buf = new StringBuilder();

        buf.append("detach-storagedomain");
        buf.append(" -datacenterid " + PowerShellUtils.escape(id));
        buf.append(" -storagedomainid " + PowerShellUtils.escape(storageDomainId));

        PowerShellCmd.runCommand(getPool(), buf.toString());
    }

    @Override
    public AttachmentResource getAttachmentSubResource(String id) {
        return new PowerShellAttachmentResource(id, storageDomainId, executor, this, shellPools, getParser());
    }
}

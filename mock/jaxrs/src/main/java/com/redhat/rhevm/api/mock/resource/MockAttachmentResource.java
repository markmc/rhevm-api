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
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.common.resource.AttachmentActionValidator;
import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.ActionsBuilder;
import com.redhat.rhevm.api.model.ActionValidator;
import com.redhat.rhevm.api.model.Attachment;
import com.redhat.rhevm.api.model.DataCenter;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.StorageDomainStatus;
import com.redhat.rhevm.api.resource.AttachmentResource;


public class MockAttachmentResource extends AbstractMockResource<Attachment> implements AttachmentResource {

    /**
     * Package-protected ctor, never needs to be instantiated by JAX-RS framework.
     *
     * @param attachment  encapsulated Attachment
     * @param executor    executor used for asynchronous actions
     */
    MockAttachmentResource(Attachment attachment, Executor executor) {
        super(attachment.getDataCenter().getId(), executor);
        attachment.setStatus(StorageDomainStatus.INACTIVE);
        updateModel(attachment);
    }

    public void updateModel(Attachment attachment) {
        DataCenter dataCenter = new DataCenter();
        dataCenter.setId(attachment.getDataCenter().getId());
        getModel().setDataCenter(dataCenter);

        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(attachment.getStorageDomain().getId());
        getModel().setStorageDomain(storageDomain);

        getModel().setId(dataCenter.getId());
        getModel().setName(dataCenter.getId());
        getModel().setStatus(attachment.getStatus());
        getModel().setMaster(attachment.isMaster());
    }

    public Attachment addLinks(UriBuilder uriBuilder) {
        LinkHelper.addLinks(getModel());

        ActionValidator actionValidator = new AttachmentActionValidator(getModel());
        ActionsBuilder actionsBuilder = new ActionsBuilder(uriBuilder, AttachmentResource.class, actionValidator);
        getModel().setActions(actionsBuilder.build());

        return getModel();
    }

    @Override
    public Attachment get(UriInfo uriInfo) {
        return addLinks(uriInfo.getRequestUriBuilder());
    }

    @Override
    public Response activate(UriInfo uriInfo, Action action) {
        // FIXME: error if not attached
        return doAction(uriInfo, new AttachmentStatusSetter(action, StorageDomainStatus.ACTIVE));
    }

    @Override
    public Response deactivate(UriInfo uriInfo, Action action) {
        // FIXME: error if not active
        return doAction(uriInfo, new AttachmentStatusSetter(action, StorageDomainStatus.INACTIVE));
    }

    private class AttachmentStatusSetter extends AbstractActionTask {
        private StorageDomainStatus status;
        public AttachmentStatusSetter(Action action, StorageDomainStatus status) {
            super(action);
            this.status = status;
        }
        public void run() {
            MockAttachmentResource.this.getModel().getStorageDomain().setStatus(status);
        }
    }
}

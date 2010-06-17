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
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.ActionsBuilder;
import com.redhat.rhevm.api.model.Attachments;
import com.redhat.rhevm.api.model.DataCenter;
import com.redhat.rhevm.api.resource.DataCenterResource;
import com.redhat.rhevm.api.resource.IsosResource;
import com.redhat.rhevm.api.common.util.JAXBHelper;
import com.redhat.rhevm.api.common.util.LinkHelper;


public class MockDataCenterResource extends AbstractMockResource<DataCenter> implements DataCenterResource {
    /* FIXME: would like to do:
     * private @Context UriInfo uriInfo;
     */

    /**
     * Package-protected ctor, never needs to be instantiated by JAX-RS framework.
     *
     * @param dataCenter  encapsulated DataCenter
     * @param executor    executor used for asynchronous actions
     */
    MockDataCenterResource(String id, Executor executor) {
        super(id, executor);
    }

    // FIXME: this needs to be atomic
    public void updateModel(DataCenter dataCenter) {
        // update writable fields only
        if (dataCenter.getName() != null) {
            getModel().setName(dataCenter.getName());
        }
        if (dataCenter.getDescription() != null) {
            getModel().setDescription(dataCenter.getDescription());
        }
        if (dataCenter.getStorageType() != null) {
            getModel().setStorageType(dataCenter.getStorageType());
        }
    }

    public DataCenter addLinks() {
        DataCenter dataCenter = JAXBHelper.clone(OBJECT_FACTORY.createDataCenter(getModel()));

        Attachments attachments = MockStorageDomainsResource.getAttachmentsForDataCenter(dataCenter.getId());
        dataCenter.setAttachments(attachments);

        return LinkHelper.addLinks(dataCenter);
    }

    /* FIXME: kill uriInfo param, make href auto-generated? */
    @Override
    public DataCenter get(UriInfo uriInfo) {
        return addLinks();
    }

    @Override
    public DataCenter update(UriInfo uriInfo, DataCenter dataCenter) {
        validateUpdate(dataCenter);
        updateModel(dataCenter);
        return addLinks();
    }

    public IsosResource getIsosResource() {
        return null;
    }
}

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

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.ActionsBuilder;
import com.redhat.rhevm.api.model.Attachments;
import com.redhat.rhevm.api.model.DataCenter;
import com.redhat.rhevm.api.resource.DataCenterResource;
import com.redhat.rhevm.api.common.util.JAXBHelper;


public class MockDataCenterResource extends AbstractMockResource<DataCenter> implements DataCenterResource {
    /* FIXME: would like to do:
     * private @Context UriInfo uriInfo;
     */

    /**
     * Package-protected ctor, never needs to be instantiated by JAX-RS framework.
     *
     * @param dataCenter  encapsulated DataCenter
     */
    MockDataCenterResource(String id) {
        super(id);
    }

    // FIXME: this needs to be atomic
    public void updateModel(DataCenter dataCenter) {
        // update writable fields only
        getModel().setName(dataCenter.getName());
        getModel().setStorageType(dataCenter.getStorageType());
    }

    public DataCenter addLinks(UriInfo uriInfo, UriBuilder uriBuilder) {
        DataCenter dataCenter = JAXBHelper.clone(OBJECT_FACTORY.createDataCenter(getModel()));

        dataCenter.setHref(uriBuilder.build().toString());

        ActionsBuilder actionsBuilder = new ActionsBuilder(uriBuilder, DataCenterResource.class);
        dataCenter.setActions(actionsBuilder.build());

        Attachments attachments = MockStorageDomainsResource.getAttachmentsForDataCenter(uriInfo, dataCenter.getId());
        dataCenter.setAttachments(attachments);

        return dataCenter;
    }

    /* FIXME: kill uriInfo param, make href auto-generated? */
    @Override
    public DataCenter get(UriInfo uriInfo) {
        return addLinks(uriInfo, uriInfo.getRequestUriBuilder());
    }

    @Override
    public DataCenter update(HttpHeaders headers, UriInfo uriInfo, DataCenter dataCenter) {
        validateUpdate(dataCenter, headers);
        updateModel(dataCenter);
        return addLinks(uriInfo, uriInfo.getRequestUriBuilder());
    }
}

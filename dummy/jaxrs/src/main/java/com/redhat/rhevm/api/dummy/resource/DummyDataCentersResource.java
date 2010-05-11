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

import com.redhat.rhevm.api.model.DataCenter;
import com.redhat.rhevm.api.model.DataCenters;
import com.redhat.rhevm.api.model.StorageType;
import com.redhat.rhevm.api.resource.DataCenterResource;
import com.redhat.rhevm.api.resource.DataCentersResource;

import static com.redhat.rhevm.api.dummy.resource.AbstractDummyResource.initialize;


public class DummyDataCentersResource implements DataCentersResource {
    /* FIXME: would like to do:
     * private @Context UriInfo uriInfo;
     */

    /* FIXME: synchronize access to this */
    private static HashMap<String, DummyDataCenterResource> dataCenters = new HashMap<String, DummyDataCenterResource>();

    static {
        while (dataCenters.size() < 2) {
            DataCenter dataCenter = new DataCenter();
            dataCenter.setName("dataCenter" + Integer.toString(dataCenters.size()));
            dataCenter.setStorageType((dataCenters.size() % 2) == 0 ? StorageType.ISCSI : StorageType.NFS);
            DummyDataCenterResource resource = new DummyDataCenterResource(initialize(dataCenter));
            dataCenters.put(resource.getModel().getId(), resource);
        }
    }

    public static String getHref(UriBuilder baseUriBuilder, String id) {
        return baseUriBuilder.clone().path("datacenters").path(id).build().toString();
    }

    @Override
    public DataCenters list(UriInfo uriInfo) {
        DataCenters ret = new DataCenters();

        for (DummyDataCenterResource dataCenter : dataCenters.values()) {
            String id = dataCenter.getModel().getId();
            UriBuilder uriBuilder = uriInfo.getRequestUriBuilder().path(id);
            ret.getDataCenters().add(dataCenter.addLinks(uriInfo, uriBuilder));
        }

        return ret;
    }

    @Override
    public Response add(UriInfo uriInfo, DataCenter dataCenter) {
        // update writable fields only

        DummyDataCenterResource newDataCenter = new DummyDataCenterResource(initialize(dataCenter));

        String id = newDataCenter.getModel().getId();
        dataCenters.put(id, newDataCenter);

        UriBuilder uriBuilder = uriInfo.getRequestUriBuilder().path(id);

        dataCenter = newDataCenter.addLinks(uriInfo, uriBuilder);

        return Response.created(uriBuilder.build()).entity(dataCenter).build();
    }

    @Override
    public void remove(String id) {
        dataCenters.remove(id);
    }

    @Override
    public DataCenterResource getDataCenterSubResource(UriInfo uriInfo, String id) {
        return dataCenters.get(id);
    }
}

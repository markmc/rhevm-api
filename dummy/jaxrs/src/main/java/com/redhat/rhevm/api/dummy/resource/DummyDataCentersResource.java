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
import com.redhat.rhevm.api.resource.DataCenterResource;
import com.redhat.rhevm.api.resource.DataCentersResource;
import com.redhat.rhevm.api.dummy.model.DummyDataCenter;

public class DummyDataCentersResource implements DataCentersResource {
    /* FIXME: would like to do:
     * private @Context UriInfo uriInfo;
     */

    /* FIXME: synchronize access to this */
    private static HashMap<String, DummyDataCenterResource> datacenters = new HashMap<String, DummyDataCenterResource>();

    static {
        while (datacenters.size() < 2) {
            DummyDataCenter datacenter = new DummyDataCenter();
            datacenter.jaxb.setName("datacenter" + Integer.toString(datacenters.size()));
            datacenters.put(datacenter.jaxb.getId(), new DummyDataCenterResource(datacenter));
        }
    }

    @Override
    public DataCenters list(UriInfo uriInfo) {
        DataCenters ret = new DataCenters();

        for (DummyDataCenterResource datacenter : datacenters.values()) {
            String id = datacenter.getDataCenter().getId();
            UriBuilder uriBuilder = uriInfo.getRequestUriBuilder().path(id);
            ret.getDataCenters().add(datacenter.addLinks(uriBuilder));
        }

        return ret;
    }

    @Override
    public Response add(UriInfo uriInfo, DataCenter datacenter) {
        DummyDataCenterResource newDataCenter = new DummyDataCenterResource(new DummyDataCenter(datacenter));

        String id = newDataCenter.getDataCenter().getId();
        datacenters.put(id, newDataCenter);

        UriBuilder uriBuilder = uriInfo.getRequestUriBuilder().path(id);

        datacenter = newDataCenter.addLinks(uriBuilder);

        return Response.created(uriBuilder.build()).entity(datacenter).build();
    }

    @Override
    public void remove(String id) {
        datacenters.remove(id);
    }

    @Override
    public DataCenterResource getDataCenterSubResource(UriInfo uriInfo, String id) {
        return datacenters.get(id);
    }
}

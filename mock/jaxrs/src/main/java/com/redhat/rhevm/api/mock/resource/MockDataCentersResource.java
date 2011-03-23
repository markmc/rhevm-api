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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.redhat.rhevm.api.mock.util.SimpleQueryEvaluator;
import com.redhat.rhevm.api.model.DataCenter;
import com.redhat.rhevm.api.model.DataCenters;
import com.redhat.rhevm.api.model.StorageType;
import com.redhat.rhevm.api.resource.DataCenterResource;
import com.redhat.rhevm.api.resource.DataCentersResource;

import static com.redhat.rhevm.api.mock.resource.AbstractMockResource.allocateId;


public class MockDataCentersResource extends AbstractMockQueryableResource<DataCenter> implements DataCentersResource {

    private static Map<String, MockDataCenterResource> dataCenters =
        Collections.synchronizedMap(new HashMap<String, MockDataCenterResource>());

    public MockDataCentersResource() {
        super(new SimpleQueryEvaluator<DataCenter>());
    }

    public void populate() {
        synchronized (dataCenters) {
            while (dataCenters.size() < 2) {
                MockDataCenterResource resource = new MockDataCenterResource(allocateId(DataCenter.class), getExecutor(), this);
                resource.getModel().setName("datacenter" + resource.getModel().getId());
                resource.getModel().setStorageType((dataCenters.size() % 2) == 0 ? StorageType.ISCSI.value() : StorageType.NFS.value());
                dataCenters.put(resource.getModel().getId(), resource);
            }
        }
    }

    @Override
    public DataCenters list() {
        DataCenters ret = new DataCenters();

        for (MockDataCenterResource dataCenter : dataCenters.values()) {
            if (filter(dataCenter.getModel(), getUriInfo(), DataCenter.class)) {
                ret.getDataCenters().add(dataCenter.addLinks());
            }
        }

        return ret;
    }

    @Override
    public Response add(DataCenter dataCenter) {
        MockDataCenterResource resource = new MockDataCenterResource(allocateId(DataCenter.class), getExecutor(), this);

        resource.updateModel(dataCenter);

        String id = resource.getId();
        dataCenters.put(id, resource);

        UriBuilder uriBuilder = getUriInfo().getAbsolutePathBuilder().path(id);

        dataCenter = resource.addLinks();

        return Response.created(uriBuilder.build()).entity(dataCenter).build();
    }

    @Override
    public void remove(String id) {
        dataCenters.remove(id);
    }

    @Override
    public DataCenterResource getDataCenterSubResource(String id) {
        return dataCenters.get(id);
    }
}

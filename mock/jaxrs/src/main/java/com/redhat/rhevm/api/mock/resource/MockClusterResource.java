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

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.ActionsBuilder;
import com.redhat.rhevm.api.model.DataCenter;
import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.CPU;
import com.redhat.rhevm.api.resource.ClusterResource;
import com.redhat.rhevm.api.common.util.JAXBHelper;


public class MockClusterResource extends AbstractMockResource<Cluster> implements ClusterResource {

    /**
     * Package-protected ctor, never needs to be instantiated by JAX-RS framework.
     *
     * @param cluster  encapsulated Cluster
     * @param executor    executor used for asynchronous actions
     */
    MockClusterResource(String id, Executor executor) {
        super(id, executor);
    }

    // FIXME: this needs to be atomic
    public void updateModel(Cluster cluster) {
        // update writable fields only
        if (cluster.getName() != null) {
            getModel().setName(cluster.getName());
        }
        if (cluster.getDescription() != null) {
            getModel().setDescription(cluster.getDescription());
        }

        if (cluster.getCpu() != null) {
            CPU cpu = new CPU();
            cpu.setId(cluster.getCpu().getId());
            getModel().setCpu(cpu);
        }

        if (cluster.getDataCenter() != null) {
            DataCenter dataCenter = new DataCenter();
            dataCenter.setId(cluster.getDataCenter().getId());
            getModel().setDataCenter(dataCenter);
        }
    }

    public Cluster addLinks(UriInfo uriInfo, UriBuilder uriBuilder) {
        Cluster cluster = JAXBHelper.clone(OBJECT_FACTORY.createCluster(getModel()));

        cluster.setHref(uriBuilder.build().toString());

        UriBuilder baseUriBuilder = uriInfo.getBaseUriBuilder();
        DataCenter dataCenter = cluster.getDataCenter();
        dataCenter.setHref(MockDataCentersResource.getHref(baseUriBuilder, dataCenter.getId()));

        ActionsBuilder actionsBuilder = new ActionsBuilder(uriBuilder, ClusterResource.class);
        cluster.setActions(actionsBuilder.build());

        return cluster;
    }

    @Override
    public Cluster get(UriInfo uriInfo) {
        return addLinks(uriInfo, uriInfo.getRequestUriBuilder());
    }

    @Override
    public Cluster update(HttpHeaders headers, UriInfo uriInfo, Cluster cluster) {
        validateUpdate(cluster, headers);
        updateModel(cluster);
        return addLinks(uriInfo, uriInfo.getRequestUriBuilder());
    }
}

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

import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.DataCenter;
import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.CPU;
import com.redhat.rhevm.api.resource.ClusterResource;
import com.redhat.rhevm.api.common.util.JAXBHelper;
import com.redhat.rhevm.api.common.util.LinkHelper;


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
        if (cluster.isSetName()) {
            getModel().setName(cluster.getName());
        }
        if (cluster.isSetDescription()) {
            getModel().setDescription(cluster.getDescription());
        }

        if (cluster.isSetCpu()) {
            CPU cpu = new CPU();
            cpu.setId(cluster.getCpu().getId());
            getModel().setCpu(cpu);
        }

        if (cluster.isSetDataCenter()) {
            DataCenter dataCenter = new DataCenter();
            dataCenter.setId(cluster.getDataCenter().getId());
            getModel().setDataCenter(dataCenter);
        }
    }

    public Cluster addLinks() {
        return LinkHelper.addLinks(JAXBHelper.clone(OBJECT_FACTORY.createCluster(getModel())));
    }

    @Override
    public Cluster get(UriInfo uriInfo) {
        return addLinks();
    }

    @Override
    public Cluster update(UriInfo uriInfo, Cluster cluster) {
        validateUpdate(cluster);
        updateModel(cluster);
        return addLinks();
    }
}

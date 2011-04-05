/*
 * Copyright © 2011 Red Hat, Inc.
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
import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.model.VmPool;
import com.redhat.rhevm.api.model.VmPools;
import com.redhat.rhevm.api.resource.VmPoolResource;
import com.redhat.rhevm.api.resource.VmPoolsResource;

import static com.redhat.rhevm.api.mock.resource.AbstractMockResource.allocateId;


public class MockVmPoolsResource extends AbstractMockQueryableResource<VmPool> implements VmPoolsResource {

    private static Map<String, MockVmPoolResource> pools =
        Collections.synchronizedMap(new HashMap<String, MockVmPoolResource>());

    public MockVmPoolsResource() {
        super(new SimpleQueryEvaluator<VmPool>());
    }

    public void populate() {
        synchronized (pools) {
            while (pools.size() < 3) {
                MockVmPoolResource resource = new MockVmPoolResource(allocateId(VmPool.class), getExecutor(), this);
                VmPool pool = resource.getModel();
                pool.setName("pool" + resource.getModel().getId());
                pool.setDescription("test pool " + resource.getModel().getId());
                Template template = new Template();
                template.setId(resource.getModel().getId());
                resource.getModel().setTemplate(template);
                Cluster cluster = new Cluster();
                cluster.setId(resource.getModel().getId());
                resource.getModel().setCluster(cluster);
                pools.put(pool.getId(), resource);
            }
        }
    }

    @Override
    public VmPools list() {
        VmPools ret = new VmPools();

        for (MockVmPoolResource pool : pools.values()) {
            if (filter(pool.getModel(), getUriInfo(), VmPool.class)) {
                ret.getVmPools().add(pool.addLinks());
            }
        }

        return ret;
    }

    @Override
    public Response add(VmPool pool) {
        MockVmPoolResource resource = new MockVmPoolResource(allocateId(VmPool.class), getExecutor(), this);

        resource.updateModel(pool);

        String id = resource.getId();
        pools.put(id, resource);

        UriBuilder uriBuilder = getUriInfo().getAbsolutePathBuilder().path(id);
        pool = resource.addLinks();

        return Response.created(uriBuilder.build()).entity(pool).build();
    }

    @Override
    public void remove(String id, VmPool pool) {
        pools.remove(id);
    }

    @Override
    public VmPoolResource getVmPoolSubResource(String id) {
        return pools.get(id);
    }
}

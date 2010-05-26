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
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.mock.util.SimpleQueryEvaluator;
import com.redhat.rhevm.api.model.DataCenter;
import com.redhat.rhevm.api.model.Network;
import com.redhat.rhevm.api.model.Networks;
import com.redhat.rhevm.api.model.NetworkStatus;
import com.redhat.rhevm.api.resource.NetworkResource;
import com.redhat.rhevm.api.resource.NetworksResource;

import static com.redhat.rhevm.api.mock.resource.AbstractMockResource.allocateId;


public class MockNetworksResource extends AbstractMockQueryableResource<Network> implements NetworksResource {

    private static Map<String, MockNetworkResource> networks =
        Collections.synchronizedMap(new HashMap<String, MockNetworkResource>());

    public MockNetworksResource() {
        super(new SimpleQueryEvaluator<Network>());
    }

    public void populate() {
        synchronized (networks) {
            while (networks.size() < 4) {
                MockNetworkResource resource = new MockNetworkResource(allocateId(Network.class), getExecutor());
                resource.getModel().setName("network" + resource.getModel().getId());
                resource.getModel().setStatus((networks.size() % 2) == 0 ? NetworkStatus.OPERATIONAL : NetworkStatus.NON_OPERATIONAL);
                DataCenter dataCenter = new DataCenter();
                dataCenter.setId(resource.getModel().getId());
                resource.getModel().setDataCenter(dataCenter);
                networks.put(resource.getModel().getId(), resource);
            }
        }
    }

    @Override
    public Networks list(UriInfo uriInfo) {
        Networks ret = new Networks();

        for (MockNetworkResource network : networks.values()) {
            if (filter(network.getModel(), uriInfo, Network.class)) {
                String id = network.getModel().getId();
                UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(id);
                ret.getNetworks().add(network.addLinks(uriInfo, uriBuilder));
            }
        }

        return ret;
    }

    @Override
    public Response add(UriInfo uriInfo, Network network) {
        MockNetworkResource resource = new MockNetworkResource(allocateId(Network.class), getExecutor());

        resource.updateModel(network);

        String id = resource.getId();
        networks.put(id, resource);

        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(id);

        network = resource.addLinks(uriInfo, uriBuilder);

        return Response.created(uriBuilder.build()).entity(network).build();
    }

    @Override
    public void remove(String id) {
        networks.remove(id);
    }

    @Override
    public NetworkResource getNetworkSubResource(UriInfo uriInfo, String id) {
        return networks.get(id);
    }
}

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
import com.redhat.rhevm.api.model.Network;
import com.redhat.rhevm.api.resource.NetworkResource;
import com.redhat.rhevm.api.common.util.JAXBHelper;


public class MockNetworkResource extends AbstractMockResource<Network> implements NetworkResource {

    /**
     * Package-protected ctor, never needs to be instantiated by JAX-RS framework.
     *
     * @param network     encapsulated network
     * @param executor executor used for asynchronous actions
     */
    MockNetworkResource(String id, Executor executor) {
        super(id, executor);
    }

    // FIXME: this needs to be atomic
    public void updateModel(Network network) {
        // update writable fields only
        getModel().setName(network.getName());
    }

    public Network addLinks(UriInfo uriInfo, UriBuilder uriBuilder) {
        Network network = JAXBHelper.clone(OBJECT_FACTORY.createNetwork(getModel()));

        network.setHref(uriBuilder.build().toString());

        UriBuilder baseUriBuilder = uriInfo.getBaseUriBuilder();
        DataCenter dataCenter = network.getDataCenter();
        dataCenter.setHref(MockDataCentersResource.getHref(baseUriBuilder, dataCenter.getId()));

        ActionsBuilder actionsBuilder = new ActionsBuilder(uriBuilder, NetworkResource.class);
        network.setActions(actionsBuilder.build());

        return network;
    }

    @Override
    public Network get(UriInfo uriInfo) {
        return addLinks(uriInfo, uriInfo.getRequestUriBuilder());
    }

    @Override
    public Network update(HttpHeaders headers, UriInfo uriInfo, Network network) {
        validateUpdate(network, headers);
        updateModel(network);
        return addLinks(uriInfo, uriInfo.getRequestUriBuilder());
    }
}
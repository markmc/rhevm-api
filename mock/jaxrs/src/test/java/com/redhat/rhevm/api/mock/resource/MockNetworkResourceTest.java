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

import org.jboss.resteasy.client.ClientResponseFailure;
import org.junit.Test;

import com.redhat.rhevm.api.model.Fault;
import com.redhat.rhevm.api.model.Network;
import com.redhat.rhevm.api.model.Networks;

public class MockNetworkResourceTest extends MockTestBase {
    private MockTestBase.NetworksResource getService() {
        return createNetworksResource(getEntryPoint("networks").getHref());
    }

    private void checkNetwork(Network network) {
        assertNotNull(network.getName());
        assertNotNull(network.getId());
        assertNotNull(network.getHref());
        assertTrue(network.getHref().endsWith("/networks/" + network.getId()));
        assertNotNull(network.getDataCenter());
        assertNotNull(network.getDataCenter().getId());
        assertNotNull(network.getDataCenter().getHref());
        assertNotNull(network.getActions());
        assertEquals(network.getActions().getLinks().size(), 0);
    }

    @Test
    public void testGetNetworksList() throws Exception {
        MockTestBase.NetworksResource service = getService();
        assertNotNull(service);

        Networks networks = service.list();
        assertNotNull(networks);
        assertTrue(networks.getNetworks().size() > 0);

        for (Network network : networks.getNetworks()) {
            checkNetwork(network);

            Network t = service.get(network.getId());
            checkNetwork(t);
            assertEquals(network.getId(), t.getId());
        }
    }

    @Test
    public void testNetworkGoodUpdate() throws Exception {
        MockTestBase.NetworksResource service = getService();
        assertNotNull(service);

        Network update = new Network();
        update.setName("wonga");
        Network updated = service.update("1", update);
        assertNotNull(updated);
        assertEquals(updated.getName(), "wonga");
        checkNetwork(updated);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testNetworkBadUpdate() throws Exception {
        MockTestBase.NetworksResource service = getService();
        assertNotNull(service);

        Network update = new Network();
        update.setId("fluffy");
        try {
            service.update("2", update);
            fail("expected ClientResponseFailure");
        } catch (ClientResponseFailure cfe) {
            assertEquals(409, cfe.getResponse().getStatus());
            // ClientResponseFailure should really support a
            // <T> ClientResponse<T> getResponse(Class<T> clz)
            // style of accessor for the response to avoid the unchecked warnin
            Fault fault = (Fault)cfe.getResponse().getEntity(Fault.class);
            assertNotNull(fault);
            assertEquals("Broken immutability constraint", fault.getReason());
            assertEquals("Attempt to set immutable field: id", fault.getDetail());
        }
    }
}

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
import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.Clusters;

public class MockClusterResourceTest extends MockTestBase {
    private MockTestBase.ClustersResource getService() {
        return createClustersResource(getEntryPoint("clusters").getHref());
    }

    private void checkCluster(Cluster cluster) {
        assertNotNull(cluster.getName());
        assertNotNull(cluster.getId());
        assertNotNull(cluster.getHref());
        assertTrue(cluster.getHref().endsWith("/clusters/" + cluster.getId()));
    }

    @Test
    public void testGetClustersList() throws Exception {
        MockTestBase.ClustersResource service = getService();
        assertNotNull(service);

        Clusters clusters = service.list(null);
        assertNotNull(clusters);
        assertTrue(clusters.getClusters().size() > 0);

        for (Cluster cluster : clusters.getClusters()) {
            checkCluster(cluster);

            Cluster t = service.get(cluster.getId());
            checkCluster(t);
            assertEquals(cluster.getId(), t.getId());
        }
    }

    @Test
    public void testGetClustersQuery() throws Exception {
        MockTestBase.ClustersResource service = getService();
        assertNotNull(service);

        Clusters clusters = service.list("name=*1");
        assertNotNull(clusters);
        assertEquals("unepected number of query matches", 1, clusters.getClusters().size());

        Cluster cluster = clusters.getClusters().get(0);
        checkCluster(cluster);

        Cluster t = service.get(cluster.getId());
        checkCluster(t);
        assertEquals(cluster.getId(), t.getId());
    }

    @Test
    public void testClusterGoodUpdate() throws Exception {
        MockTestBase.ClustersResource service = getService();
        assertNotNull(service);

        Cluster update = new Cluster();
        update.setName("wonga");
        Cluster updated = service.update("1", update);
        assertNotNull(updated);
        assertEquals(updated.getName(), "wonga");
        checkCluster(updated);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testClusterBadUpdate() throws Exception {
        MockTestBase.ClustersResource service = getService();
        assertNotNull(service);

        Cluster update = new Cluster();
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

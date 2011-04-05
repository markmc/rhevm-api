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

import org.junit.Test;

import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.model.VmPool;
import com.redhat.rhevm.api.model.VmPools;

public class MockVmPoolsResourceTest extends MockTestBase {
    private MockTestBase.VmPoolsResource getService() {
        return createVmPoolsResource(getEntryPoint("vmpools").getHref());
    }

    private void checkVmPool(VmPool vmpool) {
        assertNotNull("Pool is null", vmpool);
        assertNotNull("Id is missing", vmpool.getId());
        assertNotNull("Name is missing", vmpool.getName());
        assertNotNull("Template is missing", vmpool.getTemplate());
        assertNotNull("Template Id is missing", vmpool.getTemplate().getId());
        assertNotNull("Cluster is missing", vmpool.getCluster());
        assertNotNull("Cluster Id is missing", vmpool.getCluster().getId());
        assertNotNull("Resource uri is missing", vmpool.getHref());
        assertTrue("Resource uri is incorrect", vmpool.getHref().endsWith("vmpools/" + vmpool.getId()));
    }

    @Test
    public void testGetVmPoolsList() throws Exception {
        MockTestBase.VmPoolsResource service = getService();
        assertNotNull(service);

        VmPools vmpools = service.list(null);
        assertNotNull(vmpools);
        assertTrue(vmpools.getVmPools().size() > 0);

        for (VmPool vmpool : vmpools.getVmPools()) {
            checkVmPool(vmpool);

            VmPool t = service.get(vmpool.getId());
            checkVmPool(t);
            assertEquals(vmpool.getId(), t.getId());
        }
    }

    @Test
    public void testGetVmPoolsQuery() throws Exception {
        MockTestBase.VmPoolsResource service = getService();
        assertNotNull(service);

        VmPools vmpools = service.list("name=*1");
        assertNotNull(vmpools);
        assertEquals("unepected number of query matches", 1, vmpools.getVmPools().size());

        VmPool vmpool = vmpools.getVmPools().get(0);
        checkVmPool(vmpool);

        VmPool t = service.get(vmpool.getId());
        checkVmPool(t);
        assertEquals(vmpool.getId(), t.getId());
    }

    @Test
    public void testAddVmPool() throws Exception {
        MockTestBase.VmPoolsResource service = getService();
        assertNotNull(service);

        VmPools vmpools = service.list(null);
        assertNotNull(vmpools);
        assertTrue(vmpools.getVmPools().size() > 0);

        VmPool vmpool = new VmPool();
        String id = new Integer(vmpools.getVmPools().size() + 1).toString();
        vmpool.setName("vmpool" + id);
        vmpool.setDescription("test vmpool");
        Template template = new Template();
        template.setId(id);
        vmpool.setTemplate(template);
        Cluster cluster = new Cluster();
        cluster.setId(id);
        vmpool.setCluster(cluster);

        VmPool result = service.add(vmpool);
        checkVmPool(result);
        assertEquals("Id's dont match", id, result.getId());
        assertEquals("Names dont match", vmpool.getName(), result.getName());
        assertEquals("Desc's dont match", vmpool.getDescription(), result.getDescription());
    }

    @Test
    public void testRemoveVmPools() throws Exception {
        MockTestBase.VmPoolsResource service = getService();
        assertNotNull(service);

        VmPools vmpools = service.list(null);
        assertNotNull(vmpools);
        assertTrue(vmpools.getVmPools().size() > 0);

        for (VmPool vmpool : vmpools.getVmPools()) {
            service.remove(vmpool.getId(), new VmPool());
        }

        vmpools = service.list(null);
        assertNotNull(vmpools);
        assertTrue(vmpools.getVmPools().size() == 0);
    }
}

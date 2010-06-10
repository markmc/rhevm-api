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
package com.redhat.rhevm.api.powershell.model;

import org.junit.Test;

import java.util.ArrayList;

import com.redhat.rhevm.api.model.VmPool;


public class PowerShellVmPoolTest extends PowerShellModelTest {

    private void testVmPool(VmPool v, String id, String name, String description, Integer size, String clusterName, String templateName) {
        assertEquals(v.getId(), id);
        assertEquals(v.getName(), name);
        assertEquals(v.getDescription(), description);
        assertEquals(v.getSize(), size);
        assertNotNull(v.getCluster());
        assertEquals(v.getCluster().getName(), clusterName);
        assertNotNull(v.getTemplate());
        assertEquals(v.getTemplate().getName(), templateName);
    }

    @Test
    public void testParse() {
        String data = readFileContents("vmpool.data");
        assertNotNull(data);

        ArrayList<VmPool> pools = PowerShellVmPool.parse(data);

        assertEquals(pools.size(), 1);

        VmPool pool = pools.get(0);

        testVmPool(pool, "0", "test", "Testing, Testing", 1, "Default", "foo");
    }
}

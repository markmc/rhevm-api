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

import java.util.List;

import com.redhat.rhevm.api.model.VmPool;


public class PowerShellVmPoolTest extends PowerShellModelTest {

    private void testVmPool(VmPool v, String id, String name, String description, Integer size, String clusterName, String templateName) {
        assertEquals(id, v.getId());
        assertEquals(name, v.getName());
        assertEquals(description, v.getDescription());
        assertEquals(size, v.getSize());
        assertNotNull(v.getCluster());
        assertEquals(clusterName, v.getCluster().getName());
        assertNotNull(v.getTemplate());
        assertEquals(templateName, v.getTemplate().getName());
    }

    @Test
    public void testParse() throws Exception {
        String data = readFileContents("vmpool.xml");
        assertNotNull(data);

        List<VmPool> pools = PowerShellVmPool.parse(getParser(), data);

        assertEquals(pools.size(), 1);

        VmPool pool = pools.get(0);

        testVmPool(pool, "1de2d827-4569-47cb-a3c7-643124ecc083", "foo", null, 2, "Default", "tmpl1");
    }
}

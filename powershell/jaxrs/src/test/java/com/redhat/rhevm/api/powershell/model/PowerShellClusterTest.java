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

import com.redhat.rhevm.api.model.Cluster;


public class PowerShellClusterTest extends PowerShellModelTest {

    private void testCluster(Cluster c, String id, String name, String description, String cpuName, String dataCenterId) {
        assertEquals(c.getId(), id);
        assertEquals(c.getName(), name);
        assertEquals(c.getDescription(), description);
        assertNotNull(c.getCpu());
        assertEquals(c.getCpu().getId(), cpuName);
        assertNotNull(c.getDataCenter());
        assertEquals(c.getDataCenter().getId(), dataCenterId);
    }

    @Test
    public void testParse() {
        String data = readFileContents("cluster.data");
        assertNotNull(data);

        ArrayList<Cluster> clusters = PowerShellCluster.parse(data);

        assertEquals(clusters.size(), 2);

        testCluster(clusters.get(0), "0", "Default", "The default server cluster", "Intel Xeon 45nm Core2", "c116abad-99ce-4888-ad7f-5c7f8d7a75f2");
        testCluster(clusters.get(1), "2", "foo",     null,                         "Intel Xeon",            "c116abad-99ce-4888-ad7f-5c7f8d7a75f2");
    }
}

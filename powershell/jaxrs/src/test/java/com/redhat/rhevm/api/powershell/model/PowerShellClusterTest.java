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

import com.redhat.rhevm.api.model.Cluster;


public class PowerShellClusterTest extends PowerShellModelTest {

    private void testCluster(Cluster c, String id, String name, String description, String cpuName, String dataCenterId, int major, int minor, int overcommit) {
        assertEquals(c.getId(), id);
        assertEquals(c.getName(), name);
        assertEquals(c.getDescription(), description);
        assertNotNull(c.getCpu());
        assertEquals(c.getCpu().getId(), cpuName);
        assertNotNull(c.getDataCenter());
        assertEquals(c.getDataCenter().getId(), dataCenterId);
        assertNotNull(c.getVersion());
        assertEquals(major, c.getVersion().getMajor());
        assertEquals(minor, c.getVersion().getMinor());
        assertNotNull(c.getMemoryPolicy());
        assertNotNull(c.getMemoryPolicy().getOverCommit());
        assertEquals(overcommit, c.getMemoryPolicy().getOverCommit().getPercent());
    }

    @Test
    public void testParse() throws Exception {
        String data = readFileContents("cluster.xml");
        assertNotNull(data);

        List<Cluster> clusters = PowerShellCluster.parse(getParser(), data);

        assertEquals(clusters.size(), 1);

        testCluster(clusters.get(0), "99408929-82cf-4dc7-a532-9d998063fa95", "Default", "The default server cluster", "Intel Xeon 45nm Core2", "bb0fd622-5b2a-4c69-bfc5-29493932844a", 2, 2, 200);
    }
}

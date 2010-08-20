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

import com.redhat.rhevm.api.model.HostNIC;

public class PowerShellHostNicTest extends PowerShellModelTest {

    private static final String HOST_ID = "439c0c13-3e0a-489e-a514-1b07232ace41";

    private void testHostnic(HostNIC n, String id, String name, String hostId, String network, String macAddress, String vlanId, String ipAddress, String ipNetmask, String ipGateway) {
        assertEquals(id, n.getId());
        assertEquals(name, n.getName());
        assertNotNull(n.getHost());
        assertEquals(hostId, n.getHost().getId());
        assertNotNull(n.getNetwork());
        assertEquals(network, n.getNetwork().getName());
        if (macAddress != null) {
            assertNotNull(n.getMac());
            assertEquals(macAddress, n.getMac().getAddress());
        } else {
            assertNull(n.getMac());
        }
        if (vlanId != null) {
            assertNotNull(n.getVlan());
            assertEquals(n.getVlan().getId(), vlanId);
        } else {
            assertNull(n.getVlan());
        }
        if (ipAddress != null || ipNetmask != null || ipGateway != null) {
            assertNotNull(n.getIp());
            assertEquals(ipAddress, n.getIp().getAddress());
            assertEquals(ipNetmask, n.getIp().getNetmask());
            assertEquals(ipGateway, n.getIp().getGateway());
        } else {
            assertNull(n.getIp());
        }
    }

    @Test
    public void testParseHostNics() throws Exception {
        String data = readFileContents("hostnic.xml");
        assertNotNull(data);

        List<HostNIC> nics = PowerShellHostNIC.parse(getParser(), HOST_ID, data);

        assertNotNull(nics);
        assertEquals(1, nics.size());

        testHostnic(nics.get(0), "1343fa36-4a42-4502-a087-963123f4f52a", "eth0", HOST_ID, "rhevm", "00:31:20:5F:F9:DD", null, "192.168.1.7", "255.255.255.0", null);
    }
}

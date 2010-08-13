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

import com.redhat.rhevm.api.model.NIC;
import com.redhat.rhevm.api.model.NicType;

public class PowerShellNicTest extends PowerShellModelTest {

    private static final String VM_ID = "439c0c13-3e0a-489e-a514-1b07232ace41";

    private void testNic(NIC n, String id, String name, String vmId, String network, NicType type, String macAddress, String ipAddress, String ipNetmask, String ipGateway) {
        assertEquals(id, n.getId());
        assertEquals(name, n.getName());
        assertNotNull(n.getVm());
        assertEquals(vmId, n.getVm().getId());
        assertNotNull(n.getNetwork());
        assertEquals(network, n.getNetwork().getName());
        assertEquals(type, n.getType());
        if (macAddress != null) {
            assertNotNull(n.getMac());
            assertEquals(macAddress, n.getMac().getAddress());
        } else {
            assertNull(n.getMac());
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
    public void testParseNics() throws Exception {
        String data = readFileContents("nics.xml");
        assertNotNull(data);

        List<NIC> nics = PowerShellNIC.parse(getParser(), VM_ID, data);

        assertNotNull(nics);
        assertEquals(1, nics.size());

        testNic(nics.get(0), "a34b8c24-f1cf-4b67-9912-3b04e9ce0a7b", "nic1", VM_ID, "rhevm", NicType.RTL_8139_PV, "00:1a:4a:16:84:02", null, null, null);
    }
}

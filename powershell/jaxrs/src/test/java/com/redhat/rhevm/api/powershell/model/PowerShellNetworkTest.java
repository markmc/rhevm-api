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

import com.redhat.rhevm.api.model.Network;
import com.redhat.rhevm.api.model.NetworkStatus;


public class PowerShellNetworkTest extends PowerShellModelTest {

    private void testNetwork(Network n, String id, String name, String description, String dataCenterId, String ipAddress, String ipNetmask, String ipGateway, String vlanId, Boolean stp, NetworkStatus status) {
        assertEquals(n.getId(), id);
        assertEquals(n.getName(), name);
        assertEquals(n.getDescription(), description);
        assertNotNull(n.getDataCenter());
        assertEquals(n.getDataCenter().getId(), dataCenterId);
        if (ipAddress != null || ipNetmask != null || ipGateway != null) {
            assertNotNull(n.getIp());
            assertEquals(n.getIp().getAddress(), ipAddress);
            assertEquals(n.getIp().getNetmask(), ipNetmask);
            assertEquals(n.getIp().getGateway(), ipGateway);
        } else {
            assertNull(n.getIp());
        }
        if (vlanId != null) {
            assertNotNull(n.getVlan());
            assertEquals(n.getVlan().getId(), vlanId);
        } else {
            assertNull(n.getVlan());
        }
        assertEquals(n.isStp(), stp);
        assertEquals(n.getStatus(), status);
    }

    @Test
    public void testParse() {
        String data = readFileContents("network.data");
        assertNotNull(data);

        ArrayList<Network> networks = PowerShellNetwork.parse(data);

        assertEquals(networks.size(), 1);

        testNetwork(networks.get(0), "00000000-0000-0000-0000-000000000009", "rhevm", "Management Network", "c116abad-99ce-4888-ad7f-5c7f8d7a75f2", null, null, null, null, null, NetworkStatus.OPERATIONAL);
    }
}

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

import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.model.HostStatus;


public class PowerShellHostTest extends PowerShellModelTest {

    private void testHost(Host h, String id, String name, HostStatus status) {
        assertEquals(h.getId(), id);
        assertEquals(h.getName(), name);
        assertEquals(h.getStatus(), status);
    }

    @Test
    public void testParse() {
        String data = readFileContents("host.data");
        assertNotNull(data);

        ArrayList<Host> hosts = PowerShellHost.parse(data);

        assertEquals(hosts.size(), 1);

        testHost(hosts.get(0), "0", "zig", HostStatus.UP);
    }
}

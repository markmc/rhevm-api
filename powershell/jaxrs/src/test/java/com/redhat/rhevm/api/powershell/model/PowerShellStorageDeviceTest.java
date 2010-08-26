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
import com.redhat.rhevm.api.model.StorageType;

public class PowerShellStorageDeviceTest extends PowerShellModelTest {

    private void testStorageDevice(PowerShellStorageDevice device, String id, StorageType type) {
        assertEquals(id, device.getId());
        assertEquals(type, device.getType());
    }

    @Test
    public void testParse() throws Exception {
        String data = readFileContents("storagedevices.xml");
        assertNotNull(data);

        List<PowerShellStorageDevice> devices = PowerShellStorageDevice.parse(getParser(), data);

        assertEquals(devices.size(), 3);

        testStorageDevice(devices.get(0), "1IET_0001003f", null);
        testStorageDevice(devices.get(1), "1IET_0001003e", null);
        testStorageDevice(devices.get(2), "1IET_0001003d", null);
    }
}

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

import com.redhat.rhevm.api.model.DataCenter;
import com.redhat.rhevm.api.model.StorageType;


public class PowerShellDataCenterTest extends PowerShellModelTest {

    private void testDataCenter(DataCenter d, String id, String name, String description, StorageType type) {
        assertEquals(d.getId(), id);
        assertEquals(d.getName(), name);
        assertEquals(d.getDescription(), description);
        assertEquals(d.getStorageType(), type);
    }

    @Test
    public void testParse() {
        String data = readFileContents("datacenter.data");
        assertNotNull(data);

        ArrayList<DataCenter> dataCenters = PowerShellDataCenter.parse(data);

        assertEquals(dataCenters.size(), 1);

        testDataCenter(dataCenters.get(0), "c116abad-99ce-4888-ad7f-5c7f8d7a75f2", "Default", "The default Data Center", StorageType.NFS);
    }
}

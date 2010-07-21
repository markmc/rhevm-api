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

import com.redhat.rhevm.api.model.DataCenter;
import com.redhat.rhevm.api.model.StorageType;


public class PowerShellDataCenterTest extends PowerShellModelTest {

    private void testDataCenter(DataCenter d, String id, String name, String description, StorageType type) {
        assertEquals(id, d.getId());
        assertEquals(name, d.getName());
        assertEquals(description, d.getDescription());
        assertEquals(type, d.getStorageType());
    }

    @Test
    public void testParse() throws Exception {
        String data = readFileContents("datacenter.xml");
        assertNotNull(data);

        List<DataCenter> dataCenters = PowerShellDataCenter.parse(getParser(), data);

        assertEquals(dataCenters.size(), 1);

        testDataCenter(dataCenters.get(0), "bb0fd622-5b2a-4c69-bfc5-29493932844a", "Default", "The default Data Center", StorageType.NFS);
    }
}

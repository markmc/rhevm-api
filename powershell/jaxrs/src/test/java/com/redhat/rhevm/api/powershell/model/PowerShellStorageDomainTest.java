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

import com.redhat.rhevm.api.model.Storage;
import com.redhat.rhevm.api.model.StorageDomainStatus;
import com.redhat.rhevm.api.model.StorageDomainType;
import com.redhat.rhevm.api.model.StorageType;
import com.redhat.rhevm.api.powershell.model.PowerShellStorageDomain;


public class PowerShellStorageDomainTest extends PowerShellModelTest {

    private void testStorageDomain(PowerShellStorageDomain s, String id, String name, StorageDomainType type, Boolean isMaster, StorageDomainStatus status, StorageDomainStatus sharedStatus) {
        assertEquals(s.getId(), id);
        assertEquals(s.getName(), name);
        assertEquals(s.getType(), type);
        assertEquals(s.isMaster(), isMaster);
        assertEquals(s.getStatus(), status);
        assertEquals(s.getSharedStatus(), sharedStatus);
    }

    private void testNfsStorageDomain(PowerShellStorageDomain s, String id, String name, StorageDomainType type, Boolean isMaster, StorageDomainStatus status, StorageDomainStatus sharedStatus, String host, String path) {
        testStorageDomain(s, id, name, type, isMaster, status, sharedStatus);

        Storage storage = s.getStorage();
        assertNotNull(storage);

        assertEquals(storage.getType(), StorageType.NFS);
        assertEquals(storage.getHost(), host);
        assertEquals(storage.getPath(), path);
    }

    @Test
    public void testParse() {
        String data = readFileContents("storagedomain.data");
        assertNotNull(data);

        ArrayList<PowerShellStorageDomain> storageDomains = PowerShellStorageDomain.parse(data);

        assertEquals(storageDomains.size(), 4);

        testNfsStorageDomain(storageDomains.get(0),
                             "749ce52b-555c-4725-83b9-0cb2cc5303a9", "images_0",
                             StorageDomainType.DATA, true,  null, StorageDomainStatus.INACTIVE,
                             "172.31.0.6", "/exports/RHEVX/images/0");

        testNfsStorageDomain(storageDomains.get(1),
                             "644448c4-4b93-44a5-8295-db6d1bb367d8", "images_1",
                             StorageDomainType.DATA, null, null, StorageDomainStatus.UNATTACHED,
                             "172.31.0.6", "/exports/RHEVX/images/1");

        testNfsStorageDomain(storageDomains.get(2),
                             "dcf38312-15d6-4e3f-a5cb-b90a4554b415", "images_8",
                             StorageDomainType.DATA, null, null, StorageDomainStatus.INACTIVE,
                             "172.31.0.6", "/exports/RHEVX/images/8");

        testNfsStorageDomain(storageDomains.get(3),
                             "5db1ae3d-1628-439e-996e-b4ad955b6480", "iso_0",
                             StorageDomainType.ISO,  null, null, StorageDomainStatus.INACTIVE,
                             "172.31.0.6", "/exports/RHEVX/iso/0");
    }
}

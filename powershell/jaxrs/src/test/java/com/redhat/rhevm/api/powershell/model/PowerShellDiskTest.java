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

import com.redhat.rhevm.api.model.DiskFormat;
import com.redhat.rhevm.api.model.DiskInterface;
import com.redhat.rhevm.api.model.DiskStatus;
import com.redhat.rhevm.api.model.DiskType;

public class PowerShellDiskTest extends PowerShellModelTest {

    private static final String VM_ID = "439c0c13-3e0a-489e-a514-1b07232ace41";

    private void testDisk(PowerShellDisk d, String id, String vmId, Long size, DiskType type, DiskStatus status, DiskInterface iface, DiskFormat format, boolean sparse, boolean bootable, boolean wipeAfterDelete, boolean propagateErrors, String vmSnapshotId, String parentId, String internalDriveMapping, String lastModified, String storageDomainId) {
        assertEquals(id, d.getId());
        assertNotNull(d.getVm());
        assertEquals(vmId, d.getVm().getId());
        assertEquals(size, d.getSize());
        assertEquals(type.value(), d.getType());
        assertEquals(status, d.getStatus());
        assertEquals(iface.value(), d.getInterface());
        assertEquals(format.value(), d.getFormat());
        assertEquals(sparse, d.isSparse());
        assertEquals(bootable, d.isBootable());
        assertEquals(wipeAfterDelete, d.isWipeAfterDelete());
        assertEquals(propagateErrors, d.isPropagateErrors());
        assertEquals(vmSnapshotId, d.getVmSnapshotId());
        assertEquals(parentId, d.getParentId());
        assertEquals(internalDriveMapping, d.getInternalDriveMapping());
        assertEquals(lastModified, d.getLastModified().toString());
        assertNotNull(d.getStorageDomain());
        assertEquals(storageDomainId, d.getStorageDomain().getId());
    }

    @Test
    public void testParseDisks() throws Exception {
        String data = readFileContents("disks.xml");
        assertNotNull(data);

        List<PowerShellDisk> disks = PowerShellDisk.parse(getParser(), VM_ID, data);

        assertNotNull(disks);
        assertEquals(disks.size(), 2);

        testDisk(disks.get(0), "222ea10f-7c0a-4302-8e80-2834b8fa681a", VM_ID, 1073741824L, DiskType.DATA, DiskStatus.OK, DiskInterface.IDE, DiskFormat.COW, true, false, false, false, "22a659ab-29a3-4160-9647-bb07753c612e", "00000000-0000-0000-0000-000000000000", "2", "2010-07-22T10:40:27.000Z", "788cd1e6-aa2b-412e-bcaf-0b37b96f00b2");
        testDisk(disks.get(1), "0e833f37-3437-44f2-a04f-6f9692882431", VM_ID, 2147483648L, DiskType.SYSTEM, DiskStatus.OK, DiskInterface.VIRTIO, DiskFormat.RAW, false, true, true, false, "1d122de8-1aa2-4b07-9d42-937333ea577d", "00000000-0000-0000-0000-000000000000", "1", "2010-07-22T10:59:42.000Z", "788cd1e6-aa2b-412e-bcaf-0b37b96f00b2");
    }
}

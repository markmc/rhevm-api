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

import com.redhat.rhevm.api.model.Disk;
import com.redhat.rhevm.api.model.DiskFormat;
import com.redhat.rhevm.api.model.DiskInterface;
import com.redhat.rhevm.api.model.DiskStatus;
import com.redhat.rhevm.api.model.DiskType;

public class PowerShellDiskTest extends PowerShellModelTest {

    private static final String VM_ID = "439c0c13-3e0a-489e-a514-1b07232ace41";

    private void testDisk(Disk d, String id, String vmId, Long size, DiskType type, DiskStatus status, DiskInterface iface, DiskFormat format, Boolean sparse, Boolean bootable, Boolean wipeAfterDelete, Boolean propagateErrors) {
        assertEquals(d.getId(), id);
        assertNotNull(d.getVm());
        assertEquals(d.getVm().getId(), vmId);
        assertEquals(d.getSize(), size);
        assertEquals(d.getType(), type);
        assertEquals(d.getStatus(), status);
        assertEquals(d.getInterface(), iface);
        assertEquals(d.getFormat(), format);
        assertEquals(d.isSparse(), sparse);
        assertEquals(d.isBootable(), bootable);
        assertEquals(d.isWipeAfterDelete(), wipeAfterDelete);
        assertEquals(d.isPropagateErrors(), propagateErrors);
    }

    @Test
    public void testParseDisks() throws Exception {
        String data = readFileContents("disks.xml");
        assertNotNull(data);

        List<Disk> disks = PowerShellDisk.parse(getParser(), VM_ID, data);

        assertNotNull(disks);
        assertEquals(disks.size(), 1);

        testDisk(disks.get(0), "0b9318b4-e426-4380-9e6a-bb7f3a38a2ce", VM_ID, 1341231104L, DiskType.SYSTEM, DiskStatus.OK, DiskInterface.IDE, DiskFormat.RAW, true, true, null, null);
    }

    @Test
    public void testParseDisks22() throws Exception {
        String data = readFileContents("disks22.xml");
        assertNotNull(data);

        List<Disk> disks = PowerShellDisk.parse(getParser(), VM_ID, data);

        assertNotNull(disks);
        assertEquals(disks.size(), 2);

        testDisk(disks.get(0), "222ea10f-7c0a-4302-8e80-2834b8fa681a", VM_ID, 1073741824L, DiskType.DATA, DiskStatus.OK, DiskInterface.IDE, DiskFormat.COW, true, null, null, null);
        testDisk(disks.get(1), "0e833f37-3437-44f2-a04f-6f9692882431", VM_ID, 2147483648L, DiskType.SYSTEM, DiskStatus.OK, DiskInterface.VIRTIO, DiskFormat.RAW, null, true, true, null);
    }
}

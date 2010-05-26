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

import com.redhat.rhevm.api.model.Disk;
import com.redhat.rhevm.api.model.DiskFormat;
import com.redhat.rhevm.api.model.DiskInterface;
import com.redhat.rhevm.api.model.DiskStatus;
import com.redhat.rhevm.api.model.DiskType;
import com.redhat.rhevm.api.model.Interface;
import com.redhat.rhevm.api.model.InterfaceType;
import com.redhat.rhevm.api.model.VM;


public class PowerShellVmTest extends PowerShellModelTest {

    private void testVM(VM v, String id, String name, String description, String clusterId) {
        assertEquals(v.getId(), id);
        assertEquals(v.getName(), name);
        assertEquals(v.getDescription(), description);
        assertEquals(v.getCluster().getId(), clusterId);
    }

    private void testDisk(Disk d, String id, Long size, DiskType type, DiskStatus status, DiskInterface iface, DiskFormat format, Boolean sparse, Boolean bootable, Boolean wipeAfterDelete, Boolean propagateErrors) {
        assertEquals(d.getId(), id);
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

    private void testInterface(Interface i, String id, String name, InterfaceType type, String macAddress, String ipAddress, String ipNetmask, String ipGateway) {
        assertEquals(i.getId(), id);
        assertEquals(i.getName(), name);
        assertEquals(i.getType(), type);
        if (macAddress != null) {
            assertNotNull(i.getMac());
            assertEquals(i.getMac().getAddress(), macAddress);
        } else {
            assertNull(i.getMac());
        }
        if (ipAddress != null || ipNetmask != null || ipGateway != null) {
            assertNotNull(i.getIp());
            assertEquals(i.getIp().getAddress(), ipAddress);
            assertEquals(i.getIp().getNetmask(), ipNetmask);
            assertEquals(i.getIp().getGateway(), ipGateway);
        } else {
            assertNull(i.getIp());
        }
    }

    @Test
    public void testParse() {
        String data = readFileContents("vm.data");
        assertNotNull(data);

        ArrayList<VM> vms = PowerShellVM.parse(data);

        assertEquals(vms.size(), 1);

        VM vm = vms.get(0);

        testVM(vm, "439c0c13-3e0a-489e-a514-1b07232ace41", "test_1", null, "0");

        data = readFileContents("disks.data");
        assertNotNull(data);

        vm = PowerShellVM.parseDisks(vm, data);

        assertNotNull(vm.getDevices());
        assertEquals(vm.getDevices().getDisks().size(), 1);

        testDisk(vm.getDevices().getDisks().get(0), "eeca0966-ad77-4a3d-a750-f3ba390446da", 683622400L, DiskType.SYSTEM, DiskStatus.OK, DiskInterface.IDE, DiskFormat.RAW, true, true, null, null);

        data = readFileContents("interfaces.data");
        assertNotNull(data);

        vm = PowerShellVM.parseInterfaces(vm, data);

        assertNotNull(vm.getDevices());
        assertEquals(vm.getDevices().getInterfaces().size(), 1);

        testInterface(vm.getDevices().getInterfaces().get(0), "5e8471ec-d8b5-431b-afcb-c74846e0019b", "eth0", InterfaceType.RTL_8139_PV, "00:1a:4a:16:84:02", null, null, null);
    }
}

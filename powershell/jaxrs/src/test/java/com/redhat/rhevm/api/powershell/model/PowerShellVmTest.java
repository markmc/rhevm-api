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

import com.redhat.rhevm.api.model.BootDevice;
import com.redhat.rhevm.api.model.Disk;
import com.redhat.rhevm.api.model.Disks;
import com.redhat.rhevm.api.model.DiskFormat;
import com.redhat.rhevm.api.model.DiskInterface;
import com.redhat.rhevm.api.model.DiskStatus;
import com.redhat.rhevm.api.model.DiskType;
import com.redhat.rhevm.api.model.NIC;
import com.redhat.rhevm.api.model.Nics;
import com.redhat.rhevm.api.model.NicType;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.model.VmStatus;


public class PowerShellVmTest extends PowerShellModelTest {

    private static final String VM_ID = "439c0c13-3e0a-489e-a514-1b07232ace41";

    private void testVM(PowerShellVM v, String id, String name, String description, VmStatus status, Long memory, int sockets, int cores, String cdIsoPath, String hostId, String clusterId, String templateId) {
        assertEquals(id, v.getId());
        assertEquals(name, v.getName());
        assertEquals(description, v.getDescription());
        assertEquals(status, v.getStatus());
        assertEquals(memory, v.getMemory());
        assertNotNull(v.getCpu());
        assertNotNull(v.getCpu().getTopology());
        assertEquals(sockets, v.getCpu().getTopology().getSockets());
        assertEquals(cores, v.getCpu().getTopology().getCores());
        assertEquals(cdIsoPath, v.getCdIsoPath());
        assertNotNull(v.getCluster());
        assertEquals(clusterId, v.getCluster().getId());
        assertNotNull(v.getTemplate());
        assertEquals(templateId, v.getTemplate().getId());
    }

    private void testBootDevices(VM vm, BootDevice ... bootDevices) {
        if (bootDevices.length == 0) {
            return;
        }
        assertNotNull(vm.getOs());
        assertEquals(vm.getOs().getBoot().size(), bootDevices.length);
        for (int i = 0; i < bootDevices.length; i++) {
            assertEquals(vm.getOs().getBoot().get(i).getDev(), bootDevices[i]);
        }
    }

    @Test
    public void testParse() throws Exception {
        String data = readFileContents("vm.xml");
        assertNotNull(data);

        List<PowerShellVM> vms = PowerShellVM.parse(getParser(), data);

        assertEquals(vms.size(), 4);

        testVM(vms.get(0), "142bf5b1-04fb-4221-9360-0f9b7dab3013", "foo-1", null, VmStatus.RUNNING, 536870912L, 1, 1, null, "5f38363b-7457-4884-831e-78c27cebb31d", "99408929-82cf-4dc7-a532-9d998063fa95", "3ee77811-f1eb-4d3f-991e-e539dbb2f1f9");
        testBootDevices(vms.get(0), BootDevice.HD);

        testVM(vms.get(1), "f9e37917-8382-486f-875d-4045be045d85", "foo-2", null, VmStatus.SHUTOFF, 536870912L, 1, 1, null, null, "99408929-82cf-4dc7-a532-9d998063fa95", "3ee77811-f1eb-4d3f-991e-e539dbb2f1f9");
        testBootDevices(vms.get(1), BootDevice.HD);

        testVM(vms.get(2), "aa0e6522-5baf-4f92-86d3-716883de4359", "test", null, VmStatus.SHUTOFF, 536870912L, 1, 1, null, null, "99408929-82cf-4dc7-a532-9d998063fa95", "00000000-0000-0000-0000-000000000000");
        testBootDevices(vms.get(2), BootDevice.HD);

        testVM(vms.get(3), "5114bb3e-a4e6-44b2-b783-b3eea7d84720", "testf13", null, VmStatus.SHUTOFF, 536870912L, 1, 1, null, null, "99408929-82cf-4dc7-a532-9d998063fa95", "00000000-0000-0000-0000-000000000000");
        testBootDevices(vms.get(3), BootDevice.HD);
    }

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
    public void testParseDisks() {
        String data = readFileContents("disks.data");
        assertNotNull(data);

        Disks disks = PowerShellVM.parseDisks(VM_ID, data);

        assertNotNull(disks);
        assertEquals(disks.getDisks().size(), 1);

        testDisk(disks.getDisks().get(0), "eeca0966-ad77-4a3d-a750-f3ba390446da", VM_ID, 683622400L, DiskType.SYSTEM, DiskStatus.OK, DiskInterface.IDE, DiskFormat.RAW, true, true, null, null);

    }

    private void testNic(NIC n, String id, String name, String vmId, String network, NicType type, String macAddress, String ipAddress, String ipNetmask, String ipGateway) {
        assertEquals(n.getId(), id);
        assertEquals(n.getName(), name);
        assertNotNull(n.getVm());
        assertEquals(n.getVm().getId(), vmId);
        assertNotNull(n.getNetwork());
        assertEquals(n.getNetwork().getName(), network);
        assertEquals(n.getType(), type);
        if (macAddress != null) {
            assertNotNull(n.getMac());
            assertEquals(n.getMac().getAddress(), macAddress);
        } else {
            assertNull(n.getMac());
        }
        if (ipAddress != null || ipNetmask != null || ipGateway != null) {
            assertNotNull(n.getIp());
            assertEquals(n.getIp().getAddress(), ipAddress);
            assertEquals(n.getIp().getNetmask(), ipNetmask);
            assertEquals(n.getIp().getGateway(), ipGateway);
        } else {
            assertNull(n.getIp());
        }
    }

    @Test
    public void testParseNics() {
        String data = readFileContents("nics.data");
        assertNotNull(data);

        Nics nics = PowerShellVM.parseNics(VM_ID, data);

        assertNotNull(nics);
        assertEquals(nics.getNics().size(), 1);

        testNic(nics.getNics().get(0), "5e8471ec-d8b5-431b-afcb-c74846e0019b", "eth0", VM_ID, "rhevm", NicType.RTL_8139_PV, "00:1a:4a:16:84:02", null, null, null);
    }
}

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

import java.math.BigDecimal;
import java.util.List;

import com.redhat.rhevm.api.model.BootDevice;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.model.VmStatus;
import com.redhat.rhevm.api.model.VmType;


public class PowerShellVmTest extends PowerShellModelTest {

    private void testVM(PowerShellVM v, String id, String name, String description, VmType type, VmStatus status, Long memory, int sockets, int cores, String cdIsoPath, String hostId, String clusterId, String templateId) {
        assertEquals(id, v.getId());
        assertEquals(name, v.getName());
        assertEquals(description, v.getDescription());
        assertEquals(type, v.getType());
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
        assertTrue(v.isSetMemoryStatistics());
        assertEquals(Long.valueOf(50L), v.getMemoryStatistics().getUtilization());
        assertTrue(v.isSetCpuStatistics());
        assertEquals(BigDecimal.valueOf(10L), v.getCpuStatistics().getUser());
        assertEquals(BigDecimal.valueOf(20L), v.getCpuStatistics().getSystem());
        assertEquals(BigDecimal.valueOf(30L), v.getCpuStatistics().getIdle());
        assertEquals(BigDecimal.valueOf(40L), v.getCpuStatistics().getLoad());
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

        testVM(vms.get(0), "142bf5b1-04fb-4221-9360-0f9b7dab3013", "foo-1", null, VmType.DESKTOP, VmStatus.UP, 536870912L, 1, 1, null, "5f38363b-7457-4884-831e-78c27cebb31d", "99408929-82cf-4dc7-a532-9d998063fa95", "3ee77811-f1eb-4d3f-991e-e539dbb2f1f9");
        testBootDevices(vms.get(0), BootDevice.HD);

        testVM(vms.get(1), "f9e37917-8382-486f-875d-4045be045d85", "foo-2", null, VmType.DESKTOP, VmStatus.DOWN, 536870912L, 1, 1, null, null, "99408929-82cf-4dc7-a532-9d998063fa95", "3ee77811-f1eb-4d3f-991e-e539dbb2f1f9");
        testBootDevices(vms.get(1), BootDevice.HD);

        testVM(vms.get(2), "aa0e6522-5baf-4f92-86d3-716883de4359", "test", null, VmType.SERVER, VmStatus.DOWN, 536870912L, 1, 1, null, null, "99408929-82cf-4dc7-a532-9d998063fa95", "00000000-0000-0000-0000-000000000000");
        testBootDevices(vms.get(2), BootDevice.HD);

        testVM(vms.get(3), "5114bb3e-a4e6-44b2-b783-b3eea7d84720", "testf13", null, VmType.DESKTOP, VmStatus.DOWN, 536870912L, 1, 1, null, null, "99408929-82cf-4dc7-a532-9d998063fa95", "00000000-0000-0000-0000-000000000000");
        testBootDevices(vms.get(3), BootDevice.HD);
    }
}

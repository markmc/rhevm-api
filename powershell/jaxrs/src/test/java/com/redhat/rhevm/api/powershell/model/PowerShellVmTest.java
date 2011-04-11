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
import java.util.EnumSet;
import java.util.List;

import com.redhat.rhevm.api.model.BootDevice;
import com.redhat.rhevm.api.model.DisplayType;
import com.redhat.rhevm.api.model.Statistic;
import com.redhat.rhevm.api.model.StatisticType;
import com.redhat.rhevm.api.model.StatisticUnit;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.model.ValueType;
import com.redhat.rhevm.api.model.VmStatus;
import com.redhat.rhevm.api.model.VmType;
import com.redhat.rhevm.api.powershell.resource.PowerShellVmsResource.Detail;

public class PowerShellVmTest extends PowerShellModelTest {

    private void testVM(PowerShellVM v, String id, String name, String description, VmType type, VmStatus status, Long memory, int sockets, int cores, String os, boolean stateless, String timezone, String defaultHostId, Boolean highlyAvailable, Integer highAvailabilityPriority, String cdIsoPath, String hostId, String clusterId, String templateId, String creationTime, String origin) {
        assertEquals(id, v.getId());
        assertEquals(name, v.getName());
        assertEquals(description, v.getDescription());
        assertEquals(type.value(), v.getType());
        assertEquals(status, v.getStatus());
        assertEquals(memory, v.getMemory());
        assertNotNull(v.getCpu());
        assertNotNull(v.getCpu().getTopology());
        assertEquals(sockets, v.getCpu().getTopology().getSockets());
        assertEquals(cores, v.getCpu().getTopology().getCores());
        assertNotNull(v.getOs());
        assertEquals(os, v.getOs().getType());
        assertEquals(stateless, v.isStateless());
        assertEquals(timezone, v.getTimezone());
        assertNotNull(v.getHighAvailability());
        if (defaultHostId != null) {
            assertNotNull(v.getPlacementPolicy());
            assertNotNull(v.getPlacementPolicy().getHost());
            assertEquals(defaultHostId, v.getPlacementPolicy().getHost().getId());
        } else {
            assertNull(v.getPlacementPolicy());
        }
        assertEquals(highlyAvailable, v.getHighAvailability().isEnabled());
        assertEquals(highAvailabilityPriority, v.getHighAvailability().getPriority());
        assertEquals(cdIsoPath, v.getCdIsoPath());
        assertNotNull(v.getCluster());
        assertEquals(clusterId, v.getCluster().getId());
        assertNotNull(v.getTemplate());
        assertEquals(templateId, v.getTemplate().getId());
        if (hostId != null) {
            assertTrue(v.isSetHost());
            assertEquals(hostId, v.getHost().getId());
        } else {
            assertFalse(v.isSetHost());
        }
        assertEquals(creationTime, v.getCreationTime().toString());
        assertEquals(origin, v.getOrigin());
        verifyStatistics(v);
    }

    private void testBootDevices(VM vm, BootDevice ... bootDevices) {
        if (bootDevices.length == 0) {
            return;
        }
        assertNotNull(vm.getOs());
        assertEquals(bootDevices.length, vm.getOs().getBoot().size());
        for (int i = 0; i < bootDevices.length; i++) {
            assertEquals(bootDevices[i].value(), vm.getOs().getBoot().get(i).getDev());
        }
    }

    private void testDisplay(VM vm, DisplayType type, Integer port, String address) {
        assertTrue(vm.isSetDisplay());
        assertEquals(type.value(), vm.getDisplay().getType());
        assertEquals(port, vm.getDisplay().getPort());
        assertEquals(address, vm.getDisplay().getAddress());
    }

    private void verifyStatistics(VM vm) {
        assertTrue(vm.isSetStatistics());
        List<Statistic> statistics = vm.getStatistics().getStatistics();
        assertNotNull(statistics);
        assertEquals(4, statistics.size());
        verifyStatistic(statistics.get(0),
                        "memory.installed",
                        "Total mem configured",
                        StatisticType.GAUGE,
                        StatisticUnit.BYTES,
                        ValueType.INTEGER,
                        1024*1024*1024L);
        verifyStatistic(statistics.get(1),
                        "memory.used",
                        "Memory used (agent)",
                        StatisticType.GAUGE,
                        StatisticUnit.BYTES,
                        ValueType.INTEGER,
                        1024*1024*1024L/2);
        verifyStatistic(statistics.get(2),
                        "cpu.current.guest",
                        "CPU used by guest",
                        StatisticType.GAUGE,
                        StatisticUnit.PERCENT,
                        ValueType.DECIMAL,
                        10L);
        verifyStatistic(statistics.get(3),
                        "cpu.current.hypervisor",
                        "CPU overhead",
                        StatisticType.GAUGE,
                        StatisticUnit.PERCENT,
                        ValueType.DECIMAL,
                        20L);
    }

    private void verifyStatistic(Statistic statistic,
                                 String name,
                                 String description,
                                 StatisticType type,
                                 StatisticUnit unit,
                                 ValueType valueType,
                                 long datum) {
        verifyStatistic(statistic, name, description, type, unit, valueType, new BigDecimal(datum));
    }

    private void verifyStatistic(Statistic statistic,
                                 String name,
                                 String description,
                                 StatisticType type,
                                 StatisticUnit unit,
                                 ValueType valueType,
                                 BigDecimal datum) {
        assertEquals(name, statistic.getName());
        assertEquals(description, statistic.getDescription());
        assertEquals(type, statistic.getType());
        assertEquals(unit, statistic.getUnit());
        assertTrue(statistic.isSetValues());
        assertEquals(valueType, statistic.getValues().getType());
        assertTrue(statistic.getValues().isSetValues());
        assertEquals(1, statistic.getValues().getValues().size());
        assertEquals(datum, statistic.getValues().getValues().get(0).getDatum());
    }

    @Test
    public void testParse() throws Exception {
        String data = readFileContents("vm.xml");
        assertNotNull(data);

        List<PowerShellVM> vms = PowerShellVM.parse(getParser(), data, EnumSet.of(Detail.STATISTICS));

        assertEquals(vms.size(), 4);

        testVM(vms.get(0), "142bf5b1-04fb-4221-9360-0f9b7dab3013", "foo-1", null, VmType.DESKTOP, VmStatus.UP, 536870912L, 1, 1, "OtherLinux", false, null, "5", true, 100, "foo.iso", "5f38363b-7457-4884-831e-78c27cebb31d", "99408929-82cf-4dc7-a532-9d998063fa95", "3ee77811-f1eb-4d3f-991e-e539dbb2f1f9", "2010-07-20T17:28:18.000Z", "rhev");
        testBootDevices(vms.get(0), BootDevice.HD);
        testDisplay(vms.get(0), DisplayType.SPICE, 5910, "192.168.1.109");

        testVM(vms.get(1), "f9e37917-8382-486f-875d-4045be045d85", "foo-2", null, VmType.DESKTOP, VmStatus.DOWN, 536870912L, 1, 1, "OtherLinux", true, null, null, false, 0, null, null, "99408929-82cf-4dc7-a532-9d998063fa95", "3ee77811-f1eb-4d3f-991e-e539dbb2f1f9", "2010-07-20T17:33:03.000Z", "vmware");
        testBootDevices(vms.get(1), BootDevice.HD);
        testDisplay(vms.get(1), DisplayType.SPICE, null, null);

        testVM(vms.get(2), "aa0e6522-5baf-4f92-86d3-716883de4359", "test", null, VmType.SERVER, VmStatus.DOWN, 536870912L, 1, 1, "WindowsXP", false, "Europe/London", null, false, 1, null, null, "99408929-82cf-4dc7-a532-9d998063fa95", "00000000-0000-0000-0000-000000000000", "2010-07-20T10:27:02.000Z", "xen");
        testBootDevices(vms.get(2), BootDevice.HD);
        testDisplay(vms.get(2), DisplayType.VNC, 5910, null);

        testVM(vms.get(3), "5114bb3e-a4e6-44b2-b783-b3eea7d84720", "testf13", null, VmType.DESKTOP, VmStatus.POWERING_DOWN, 536870912L, 1, 1, "OtherLinux", false, null, null, false, 1, null, null, "99408929-82cf-4dc7-a532-9d998063fa95", "00000000-0000-0000-0000-000000000000", "2010-07-20T10:58:41.000Z", "rhev");
        testBootDevices(vms.get(3), BootDevice.HD);
        testDisplay(vms.get(3), DisplayType.VNC, 5910, "192.168.1.108");
    }
}

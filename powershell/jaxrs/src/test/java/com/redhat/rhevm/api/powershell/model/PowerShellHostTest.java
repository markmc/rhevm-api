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

import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.model.HostState;
import com.redhat.rhevm.api.model.PowerManagement;
import com.redhat.rhevm.api.model.Statistic;
import com.redhat.rhevm.api.model.StatisticType;
import com.redhat.rhevm.api.model.StatisticUnit;
import com.redhat.rhevm.api.model.ValueType;

public class PowerShellHostTest extends PowerShellModelTest {

    private void testPowerManagement(PowerManagement p, String type, Boolean enabled, String address, String username, String[] optionNames, String[] optionValues) {
        assertEquals(type, p.getType());
        assertEquals(enabled, p.isEnabled());
        assertEquals(address, p.getAddress());
        assertEquals(username, p.getUsername());
        assertFalse(p.isSetPassword());
        assertEquals(optionNames.length, p.getOptions().getOptions().size());
        for (int i = 0; i < optionNames.length; i++) {
            assertEquals(optionNames[i], p.getOptions().getOptions().get(i).getName());
            assertEquals(optionValues[i], p.getOptions().getOptions().get(i).getValue());
            assertFalse(p.getOptions().getOptions().get(i).isSetType());
        }
    }

    private void testHost(Host h, String id, String name, HostState state, String clusterId, Boolean storageManager, String address, int port) {
        assertEquals(h.getId(), id);
        assertEquals(h.getName(), name);
        assertNotNull(h.getStatus());
        assertEquals(h.getStatus().getState(), state);
        assertNotNull(h.getStatus().getDetail());
        assertTrue(h.isSetCluster());
        assertEquals(clusterId, h.getCluster().getId());
        assertEquals(storageManager, h.isStorageManager());
        assertEquals(address, h.getAddress());
        assertEquals(Integer.valueOf(port), h.getPort());
        verifyStatistics(h);
    }

    private void verifyStatistics(Host host) {
        assertTrue(host.isSetStatistics());
        List<Statistic> statistics = host.getStatistics().getStatistics();
        assertNotNull(statistics);
        assertEquals(11, statistics.size());
        verifyStatistic(statistics.get(0),
                "memory.installed",
                "Total physical memory",
                StatisticType.GAUGE,
                StatisticUnit.BYTES,
                ValueType.INTEGER,
                4096*1024*1024L);
        verifyStatistic(statistics.get(1),
                "memory.total",
                "Total guest memory",
                StatisticType.GAUGE,
                StatisticUnit.BYTES,
                ValueType.INTEGER,
                1154*1024*1024L);
        verifyStatistic(statistics.get(2),
                "memory.used",
                "Memory used",
                StatisticType.GAUGE,
                StatisticUnit.BYTES,
                ValueType.INTEGER,
                4096*1024*1024L/2);
        verifyStatistic(statistics.get(3),
                "memory.free",
                "Memory free",
                StatisticType.GAUGE,
                StatisticUnit.BYTES,
                ValueType.INTEGER,
                4096*1024*1024L/2);
        verifyStatistic(statistics.get(4),
                "swap.total",
                "Total swap",
                StatisticType.GAUGE,
                StatisticUnit.BYTES,
                ValueType.INTEGER,
                3955*1024*1024L);
        verifyStatistic(statistics.get(5),
                "swap.free",
                "Free swap",
                StatisticType.GAUGE,
                StatisticUnit.BYTES,
                ValueType.INTEGER,
                2060*1024*1024L);
        verifyStatistic(statistics.get(6),
                "cpu.current.user",
                "User+nic CPU usage",
                StatisticType.GAUGE,
                StatisticUnit.PERCENT,
                ValueType.DECIMAL,
                10L);
        verifyStatistic(statistics.get(7),
                "cpu.current.system",
                "System CPU usage",
                StatisticType.GAUGE,
                StatisticUnit.PERCENT,
                ValueType.DECIMAL,
                20L);
        verifyStatistic(statistics.get(8),
                "cpu.current.idle",
                "Idle CPU usage",
                StatisticType.GAUGE,
                StatisticUnit.PERCENT,
                ValueType.DECIMAL,
                30L);
        verifyStatistic(statistics.get(9),
                "cpu.load.avg.5m",
                "5min CPU load average",
                StatisticType.GAUGE,
                StatisticUnit.NONE,
                ValueType.DECIMAL,
                new BigDecimal("2.1"));
        verifyStatistic(statistics.get(10),
                "ksm.cpu.current",
                "KSM CPU usage",
                StatisticType.GAUGE,
                StatisticUnit.PERCENT,
                ValueType.DECIMAL,
                5L);
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
        String data = readFileContents("host.xml");
        assertNotNull(data);

        List<Host> hosts = PowerShellHost.parse(getParser(), data);

        assertEquals(hosts.size(), 1);

        testHost(hosts.get(0), "1", "zig", HostState.UP, "0", true, "172.31.0.7", 54321);

        assertNotNull(hosts.get(0).getPowerManagement());
        testPowerManagement(hosts.get(0).getPowerManagement(), "ilo", true, "192.168.1.107", "foo", new String[] { "secure", "port" }, new String[] { "true", "12345" });
    }
}

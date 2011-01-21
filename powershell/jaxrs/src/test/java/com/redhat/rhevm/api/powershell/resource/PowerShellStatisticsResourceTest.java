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
package com.redhat.rhevm.api.powershell.resource;

import java.util.concurrent.Executor;

import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.model.Statistic;
import com.redhat.rhevm.api.model.Statistics;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.UUID;
import com.redhat.rhevm.api.resource.StatisticResource;
import com.redhat.rhevm.api.resource.StatisticsResource;

import org.junit.Test;

import org.junit.runner.RunWith;

import static org.easymock.classextension.EasyMock.expect;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { PowerShellCmd.class })
public class PowerShellStatisticsResourceTest
    extends AbstractPowerShellResourceTest<VM, PowerShellVmResource> {

    private static final String VM_ID = UUID.asId("bling");
    private static final String GET_STATS_CMD = "$vm = get-vm \"" + VM_ID + "\" ; $vm.getmemorystatistics() ; $vm.getcpustatistics()";

    private static final Object[] extraArgs = { Integer.valueOf(512), Integer.valueOf(50), Double.valueOf(33.5D), Double.valueOf(21.7D) };

    protected PowerShellVmResource getResource(Executor executor, PowerShellPoolMap poolMap, PowerShellParser parser, UriInfoProvider uriProvider) {
        return new PowerShellVmResource(VM_ID, executor, uriProvider, poolMap, parser, httpHeaders);
    }

    protected String formatStatistic() {
        return formatXmlReturn("vmStats", extraArgs);
    }

    @Test
    public void testList() {
        StatisticsResource collection = resource.getStatisticsResource();
        setUpCmdExpectations(GET_STATS_CMD, formatStatistic());
        setUriInfo(setUpBasicUriExpectations());
        replayAll();

        verifyStatistics(collection.list(), new double[] { 512*1024*1024D, 512*1024*1024/2D, 33.5D, 21.7D });
    }

    @Test
    public void testGet() {
        StatisticsResource collection = resource.getStatisticsResource();
        StatisticResource statsResource = collection.getStatisticSubResource(UUID.asId("memory.used").toString());

        setUpCmdExpectations(GET_STATS_CMD, formatStatistic());
        setUriInfo(setUpBasicUriExpectations());
        replayAll();

        verifyStatistic(statsResource.get(), 512*1024*1024/2D);
    }

    private void setUpCmdExpectations(String command, String ret) {
        mockStatic(PowerShellCmd.class);
        expect(PowerShellCmd.runCommand(setUpPoolExpectations(), command)).andReturn(ret);
    }

    private void verifyStatistics(Statistics statistics, double[] args) {
        int i = 0;
        for (Statistic statistic : statistics.getStatistics()) {
            verifyStatistic(statistic, args[i++]);
        }
    }

    private void verifyStatistic(Statistic statistic, double datum) {
        assertTrue(statistic.isSetVm());
        assertEquals(VM_ID, statistic.getVm().getId());
        assertTrue(statistic.isSetValues());
        assertTrue(statistic.getValues().isSetValues());
        assertEquals(1, statistic.getValues().getValues().size());
        assertEquals("unexpected value for: " + statistic.getName(),
                     datum,
                     statistic.getValues().getValues().get(0).getDatum(),
                     0.1D);
    }

}

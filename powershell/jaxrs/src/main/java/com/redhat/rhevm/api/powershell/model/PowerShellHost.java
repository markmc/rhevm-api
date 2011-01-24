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

import java.util.ArrayList;
import java.util.List;

import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.model.HostStatus;
import com.redhat.rhevm.api.model.PowerManagement;
import com.redhat.rhevm.api.model.PowerManagementOption;
import com.redhat.rhevm.api.model.PowerManagementOptions;
import com.redhat.rhevm.api.model.Statistic;
import com.redhat.rhevm.api.model.Statistics;
import com.redhat.rhevm.api.powershell.enums.PowerShellVdsSpmStatus;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;

import static com.redhat.rhevm.api.powershell.util.PowerShellUtils.last;

public class PowerShellHost {

    private static final String HOST_TYPE = "RhevmCmd.CLIHost";

    private static HostStatus parseStatus(String s) {
        if (s.equals("Down"))                      return HostStatus.DOWN;
        if (s.equals("Error"))                     return HostStatus.ERROR;
        if (s.equals("Initializing"))              return HostStatus.INITIALIZING;
        if (s.equals("Installing"))                return HostStatus.INSTALLING;
        if (s.equals("Install Failed"))            return HostStatus.INSTALL_FAILED;
        if (s.equals("Maintenance"))               return HostStatus.MAINTENANCE;
        if (s.equals("Non Operational"))           return HostStatus.NON_OPERATIONAL;
        if (s.equals("Non Responsive"))            return HostStatus.NON_RESPONSIVE;
        if (s.equals("Pending Approval"))          return HostStatus.PENDING_APPROVAL;
        if (s.equals("Preparing For Maintenance")) return HostStatus.PREPARING_FOR_MAINTENANCE;
        if (s.equals("Non-Responsive"))            return HostStatus.PROBLEMATIC;
        if (s.equals("Reboot"))                    return HostStatus.REBOOT;
        if (s.equals("Unassigned"))                return HostStatus.UNASSIGNED;
        if (s.equals("Up"))                        return HostStatus.UP;
        else assert false : s;
        return null;
    }

    private static PowerManagementOptions parsePowerManagementOptions(String options) {
        if (options == null) {
            return null;
         }

        PowerManagementOptions ret = new PowerManagementOptions();

        String[] opts = options.split(",");
        for (int i = 0; i < opts.length; i++) {
            String[] parts = opts[i].split("=");

            PowerManagementOption option = new PowerManagementOption();
            option.setName(parts[0]);
            option.setValue(parts[1]);
            ret.getOptions().add(option);
        }

        return ret;
    }

    private static PowerManagement parsePowerManagement(PowerShellParser.PowerManagement parsed) {
        PowerManagement ret = new PowerManagement();
        ret.setType(parsed.getType());
        ret.setEnabled(parsed.getEnabled());
        ret.setAddress(parsed.getAddress());
        ret.setUsername(parsed.getUsername());
        ret.setOptions(parsePowerManagementOptions(parsed.getOptions()));
        return ret;
    }

    public static List<Host> parse(PowerShellParser parser, String output) {
        List<Host> ret = new ArrayList<Host>();

        for (PowerShellParser.Entity entity : parser.parse(output)) {
            if (HOST_TYPE.equals(entity.getType())) {
                ret.add(parseHost(entity));
            } else if (PowerShellHostStatisticsParser.isMemory(entity)) {
                getStatistics(ret).addAll(PowerShellHostStatisticsParser.parseMemoryStats(entity));
            } else if (PowerShellHostStatisticsParser.isCpu(entity)) {
                getStatistics(ret).addAll(PowerShellHostStatisticsParser.parseCpuStats(entity));
            } else if (PowerShellHostStatisticsParser.isKsm(entity)) {
                getStatistics(ret).addAll(PowerShellHostStatisticsParser.parseKsmStats(entity));
            }
        }

        return ret;
    }

    private static Host parseHost(PowerShellParser.Entity entity) {
        Host host = new Host();
        host.setId(entity.get("hostid", String.class, Integer.class).toString());
        host.setCluster(new Cluster());
        host.getCluster().setId(entity.get("hostclusterid", String.class, Integer.class).toString());
        host.setPort(entity.get("port", Integer.class));
        host.setName(entity.get("name"));
        host.setStatus(parseStatus(entity.get("status")));
        host.setStorageManager(entity.get("spmstatus", PowerShellVdsSpmStatus.class).map());
        host.setPowerManagement(parsePowerManagement(entity.get("powermanagement",
                                                                PowerShellParser.PowerManagement.class)));
        return host;
    }

    private static List<Statistic> getStatistics(List<Host> hosts) {
        Host host = last(hosts);
        if (!host.isSetStatistics()) {
            host.setStatistics(new Statistics());
        }
        return host.getStatistics().getStatistics();
    }
}

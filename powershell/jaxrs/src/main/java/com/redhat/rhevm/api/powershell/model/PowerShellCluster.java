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
import com.redhat.rhevm.api.model.CPU;
import com.redhat.rhevm.api.model.DataCenter;
import com.redhat.rhevm.api.model.MemoryPolicy;
import com.redhat.rhevm.api.model.MemoryOverCommit;
import com.redhat.rhevm.api.model.SchedulingPolicy;
import com.redhat.rhevm.api.model.SchedulingPolicyThresholds;
import com.redhat.rhevm.api.model.SchedulingPolicyType;
import com.redhat.rhevm.api.model.Version;
import com.redhat.rhevm.api.powershell.enums.PowerShellHostSelectionAlgorithm;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;

public class PowerShellCluster {

    protected static SchedulingPolicy parseSchedulingPolicy(PowerShellParser.Entity entity) {
        SchedulingPolicyType type = entity.get("selectionalgorithm", PowerShellHostSelectionAlgorithm.class).map();

        if (type == null) {
            return null;
        }

        int low = entity.get("lowutilization", Integer.class);
        int high = entity.get("highutilization", Integer.class);
        int duration = entity.get("cpuovercommitdurationminutes", Integer.class) * 60;

        SchedulingPolicy policy = new SchedulingPolicy();
        policy.setPolicy(type.value());
        policy.setThresholds(new SchedulingPolicyThresholds());

        switch (type) {
        case POWER_SAVING:
            policy.getThresholds().setLow(low);
        case EVENLY_DISTRIBUTED:
            policy.getThresholds().setHigh(high);
            policy.getThresholds().setDuration(duration);
            break;
        default:
            break;
        }

        return policy;
    }

    public static List<Cluster> parse(PowerShellParser parser, String output) {
        List<Cluster> ret = new ArrayList<Cluster>();

        for (PowerShellParser.Entity entity : parser.parse(output)) {
            Cluster cluster = new Cluster();

            cluster.setId(entity.get("clusterid", String.class, Integer.class).toString());
            cluster.setName(entity.get("name"));
            cluster.setDescription(entity.get("description"));

            cluster.setMemoryPolicy(new MemoryPolicy());
            cluster.getMemoryPolicy().setOverCommit(new MemoryOverCommit());
            cluster.getMemoryPolicy().getOverCommit().setPercent(entity.get("maxhostmemoryovercommit", Integer.class));

            cluster.setSchedulingPolicy(parseSchedulingPolicy(entity));

            cluster.setVersion(entity.get("compatibilityversion", Version.class));

            CPU cpu = new CPU();
            cpu.setId(entity.get("cpuname"));
            cluster.setCpu(cpu);

            DataCenter dataCenter = new DataCenter();
            dataCenter.setId(entity.get("datacenterid"));
            cluster.setDataCenter(dataCenter);

            ret.add(cluster);
        }

        return ret;
    }
}

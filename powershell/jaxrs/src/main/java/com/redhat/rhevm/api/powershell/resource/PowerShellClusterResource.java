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

import java.util.List;
import java.util.concurrent.Executor;

import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.resource.ClusterResource;
import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.powershell.model.PowerShellCluster;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;


public class PowerShellClusterResource extends AbstractPowerShellActionableResource<Cluster> implements ClusterResource {

    public PowerShellClusterResource(String id,
                                     Executor executor,
                                     PowerShellPoolMap shellPools,
                                     PowerShellParser parser) {
        super(id, executor, shellPools, parser);
    }

    public static List<Cluster> runAndParse(PowerShellCmd shell, PowerShellParser parser, String command) {
        return PowerShellCluster.parse(parser, PowerShellCmd.runCommand(shell, command, true));
    }

    public static Cluster runAndParseSingle(PowerShellCmd shell, PowerShellParser parser, String command) {
        List<Cluster> clusters = runAndParse(shell, parser, command);

        return !clusters.isEmpty() ? clusters.get(0) : null;
    }

    public List<Cluster> runAndParse(String command) {
        return runAndParse(getShell(), getParser(), command);
    }

    public Cluster runAndParseSingle(String command) {
        return runAndParseSingle(getShell(), getParser(), command);
    }

    @Override
    public Cluster get(UriInfo uriInfo) {
        StringBuilder buf = new StringBuilder();

        buf.append("$l = select-cluster;");
        buf.append("foreach ($c in $l) { ");
        buf.append("  if ($c.clusterid -eq " + PowerShellUtils.escape(getId()) + ") { ");
        buf.append("    echo $c ");
        buf.append("  } ");
        buf.append("}");

        return LinkHelper.addLinks(runAndParseSingle(buf.toString()));
    }

    @Override
    public Cluster update(UriInfo uriInfo, Cluster cluster) {
        validateUpdate(cluster);

        StringBuilder buf = new StringBuilder();

        buf.append("$l = select-cluster;");
        buf.append("foreach ($c in $l) { ");
        buf.append("  if ($c.clusterid -eq " + PowerShellUtils.escape(getId()) + ") { ");

        if (cluster.getName() != null) {
            buf.append("    $c.name = " + PowerShellUtils.escape(cluster.getName()) + ";");
        }
        if (cluster.getCpu() != null) {
            buf.append("    $c.cpuname = " + PowerShellUtils.escape(cluster.getCpu().getId()) + ";");
        }
        if (cluster.getDescription() != null) {
            buf.append("    $c.description = " + PowerShellUtils.escape(cluster.getDescription()) + ";");
        }

        buf.append("    update-datacenter -datacenterobject $v;");

        buf.append("  } ");
        buf.append("}");

        return LinkHelper.addLinks(runAndParseSingle(buf.toString()));
    }
}

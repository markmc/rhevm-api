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
import com.redhat.rhevm.api.model.Link;
import com.redhat.rhevm.api.model.SupportedVersions;
import com.redhat.rhevm.api.model.Version;
import com.redhat.rhevm.api.resource.AssignedNetworksResource;
import com.redhat.rhevm.api.resource.ClusterResource;
import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.powershell.model.PowerShellCluster;
import com.redhat.rhevm.api.powershell.model.PowerShellVersion;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPool;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;


public class PowerShellClusterResource extends AbstractPowerShellActionableResource<Cluster> implements ClusterResource {

    public PowerShellClusterResource(String id,
                                     Executor executor,
                                     PowerShellPoolMap shellPools,
                                     PowerShellParser parser) {
        super(id, executor, shellPools, parser);
    }

    public static List<Cluster> runAndParse(PowerShellPool pool, PowerShellParser parser, String command) {
        return PowerShellCluster.parse(parser, PowerShellCmd.runCommand(pool, command));
    }

    public static Cluster runAndParseSingle(PowerShellPool pool, PowerShellParser parser, String command) {
        List<Cluster> clusters = runAndParse(pool, parser, command);

        return !clusters.isEmpty() ? clusters.get(0) : null;
    }

    public List<Cluster> runAndParse(String command) {
        return runAndParse(getPool(), getParser(), command);
    }

    public Cluster runAndParseSingle(String command) {
        return runAndParseSingle(getPool(), getParser(), command);
    }

    private static Cluster querySupportedVersions(PowerShellPool pool, PowerShellParser parser, Cluster cluster) {
        String command = "get-clustercompatibilityversions -clusterid " + PowerShellUtils.escape(cluster.getId());

        List<Version> supported = PowerShellVersion.parse(parser, PowerShellCmd.runCommand(pool, command));
        if (!supported.isEmpty()) {
            cluster.setSupportedVersions(new SupportedVersions());
            for (Version v : supported) {
                cluster.getSupportedVersions().getVersions().add(v);
            }
        }

        return cluster;
    }

    public static Cluster addLinks(PowerShellPool pool, PowerShellParser parser, Cluster cluster) {
        cluster = querySupportedVersions(pool, parser, cluster);

        Link link = new Link();
        link.setRel("networks");
        link.setHref(LinkHelper.getUriBuilder(cluster).path("networks").build().toString());

        cluster.getLinks().clear();
        cluster.getLinks().add(link);

        return LinkHelper.addLinks(cluster);
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

        return addLinks(getPool(), getParser(), runAndParseSingle(buf.toString()));
    }

    @Override
    public Cluster update(UriInfo uriInfo, Cluster cluster) {
        validateUpdate(cluster);

        StringBuilder buf = new StringBuilder();

        buf.append("$l = select-cluster;");
        buf.append("foreach ($c in $l) { ");
        buf.append(" if ($c.clusterid -eq " + PowerShellUtils.escape(getId()) + ") { ");

        if (cluster.getName() != null) {
            buf.append("$c.name = " + PowerShellUtils.escape(cluster.getName()) + "; ");
        }
        if (cluster.getCpu() != null) {
            buf.append("$c.cpuname = " + PowerShellUtils.escape(cluster.getCpu().getId()) + "; ");
        }
        if (cluster.getDescription() != null) {
            buf.append("$c.description = " + PowerShellUtils.escape(cluster.getDescription()) + "; ");
        }

        if (cluster.isSetVersion() &&
            cluster.getVersion().isSetMajor() &&
            cluster.getVersion().isSetMinor()) {
            Version v = cluster.getVersion();
            buf.append("foreach ($v in get-clustercompatibilityversions -clusterid ");
            buf.append(PowerShellUtils.escape(cluster.getId()));
            buf.append(") { ");
            buf.append("if (");
            buf.append("$v.major -eq " + Integer.toString(v.getMajor()));
            buf.append(" -and ");
            buf.append("$v.minor -eq " + Integer.toString(v.getMinor()));
            buf.append(") { ");
            buf.append("$c.compatibilityversion = $v; break");
            buf.append(" } } ");
        }

        buf.append("update-cluster -clusterobject $c; break ");

        buf.append("} }");

        return addLinks(getPool(), getParser(), runAndParseSingle(buf.toString()));
    }

    @Override
    public AssignedNetworksResource getAssignedNetworksSubResource() {
        return new PowerShellClusterNetworksResource(getId(), getExecutor(), shellPools, getParser());
    }
}

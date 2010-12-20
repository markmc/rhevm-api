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

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.Clusters;
import com.redhat.rhevm.api.model.SchedulingPolicy;
import com.redhat.rhevm.api.model.SchedulingPolicyThresholds;
import com.redhat.rhevm.api.model.SchedulingPolicyType;
import com.redhat.rhevm.api.model.Version;
import com.redhat.rhevm.api.resource.ClusterResource;
import com.redhat.rhevm.api.resource.ClustersResource;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;

import static com.redhat.rhevm.api.common.util.CompletenessAssertor.validateParameters;


public class PowerShellClustersResource
    extends AbstractPowerShellCollectionResource<Cluster, PowerShellClusterResource>
    implements ClustersResource {

    public List<Cluster> runAndParse(String command) {
        return PowerShellClusterResource.runAndParse(getPool(), getParser(), command);
    }

    public Cluster runAndParseSingle(String command) {
        return PowerShellClusterResource.runAndParseSingle(getPool(), getParser(), command);
    }

    @Override
    public Clusters list() {
        Clusters ret = new Clusters();
        for (Cluster cluster : runAndParse(getSelectCommand("select-cluster", getUriInfo(), Cluster.class))) {
            ret.getClusters().add(PowerShellClusterResource.addLinks(getUriInfo(), getPool(), getParser(), cluster));
        }
        return ret;
    }

    @Override
    public Response add(Cluster cluster) {
        validateParameters(cluster, "name", "dataCenter.id|name", "cpu.id");
        StringBuilder buf = new StringBuilder();

        String dataCenterArg = getDataCenterArg(buf, cluster);

        buf.append("foreach ($v in get-clustercompatibilityversions -datacenterid " + dataCenterArg + ") { ");
        if (cluster.isSetVersion() &&
            cluster.getVersion().isSetMajor() &&
            cluster.getVersion().isSetMinor()) {
            Version v = cluster.getVersion();
            buf.append("if (");
            buf.append("$v.major -eq " + Integer.toString(v.getMajor()));
            buf.append(" -and ");
            buf.append("$v.minor -eq " + Integer.toString(v.getMinor()));
            buf.append(") { ");
            buf.append("$version = $v; break");
            buf.append(" }");
        } else {
            buf.append("$version = $v; break");
        }
        buf.append(" } ");

        buf.append("add-cluster");

        buf.append(" -clustername " + PowerShellUtils.escape(cluster.getName()));
        if (cluster.getDescription() != null) {
            buf.append(" -clusterdescription " + PowerShellUtils.escape(cluster.getDescription()));
        }
        buf.append(" -clustercpuname " + PowerShellUtils.escape(cluster.getCpu().getId()));

        if (cluster.isSetMemoryPolicy() &&
            cluster.getMemoryPolicy().isSetOverCommit() &&
            cluster.getMemoryPolicy().getOverCommit().isSetPercent()) {
            buf.append(" -maxmemoryovercommit " + Integer.toString(cluster.getMemoryPolicy().getOverCommit().getPercent()));
        }

        if (cluster.isSetSchedulingPolicy() &&
            cluster.getSchedulingPolicy().isSetPolicy()) {
            SchedulingPolicyType policy = cluster.getSchedulingPolicy().getPolicy();
            SchedulingPolicyThresholds thresholds = cluster.getSchedulingPolicy().getThresholds();

            buf.append(" -selectionalgorithm ");
            if (policy == SchedulingPolicyType.POWER_SAVING) {
                buf.append("PowerSave");
                if (thresholds != null && thresholds.isSetLow()) {
                    buf.append(" -lowutilization " + Integer.toString(thresholds.getLow()));
                }
            } else {
                buf.append("EvenlyDistribute");
            }

            if (thresholds != null) {
                if (thresholds.isSetHigh()) {
                    buf.append(" -highutilization " + Integer.toString(thresholds.getHigh()));
                }
                if (thresholds.isSetDuration()) {
                    buf.append(" -cpuovercommitdurationinminutes " + Integer.toString(thresholds.getDuration() / 60));
                }
            }
        }

        buf.append(" -datacenterid " + dataCenterArg);
        buf.append(" -compatibilityversion $version");

        cluster = PowerShellClusterResource.addLinks(getUriInfo(), getPool(), getParser(), runAndParseSingle(buf.toString()));

        UriBuilder uriBuilder = getUriInfo().getAbsolutePathBuilder().path(cluster.getId());

        return Response.created(uriBuilder.build()).entity(cluster).build();
    }

    @Override
    public void remove(String id) {
        PowerShellCmd.runCommand(getPool(), "remove-cluster -clusterid " + PowerShellUtils.escape(id));
        removeSubResource(id);
    }

    @Override
    public ClusterResource getClusterSubResource(String id) {
        return getSubResource(id);
    }

    protected PowerShellClusterResource createSubResource(String id) {
        return new PowerShellClusterResource(id, getExecutor(), this, shellPools, getParser());
    }

    private String getDataCenterArg(StringBuilder buf, Cluster cluster) {
        String arg = "";
        if (cluster.isSetDataCenter()) {
            if (cluster.getDataCenter().isSetId()) {
                arg = PowerShellUtils.escape(cluster.getDataCenter().getId());
            } else if (cluster.getDataCenter().isSetName())  {
                buf.append("$d = select-datacenter -searchtext ");
                buf.append(PowerShellUtils.escape("name=" + cluster.getDataCenter().getName()));
                buf.append("; ");
                arg = "$d.datacenterid";
            }
        }
        return arg;
    }
}

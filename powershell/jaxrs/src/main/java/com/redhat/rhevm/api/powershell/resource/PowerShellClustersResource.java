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
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.Clusters;
import com.redhat.rhevm.api.model.Version;
import com.redhat.rhevm.api.resource.ClusterResource;
import com.redhat.rhevm.api.resource.ClustersResource;
import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;


public class PowerShellClustersResource
    extends AbstractPowerShellCollectionResource<Cluster, PowerShellClusterResource>
    implements ClustersResource {

    public List<Cluster> runAndParse(String command) {
        return PowerShellClusterResource.runAndParse(getShell(), getParser(), command);
    }

    public Cluster runAndParseSingle(String command) {
        return PowerShellClusterResource.runAndParseSingle(getShell(), getParser(), command);
    }

    @Override
    public Clusters list(UriInfo uriInfo) {
        Clusters ret = new Clusters();
        for (Cluster cluster : runAndParse(getSelectCommand("select-cluster", uriInfo, Cluster.class))) {
            ret.getClusters().add(LinkHelper.addLinks(cluster));
        }
        return ret;
    }

    @Override
    public Response add(UriInfo uriInfo, Cluster cluster) {
        StringBuilder buf = new StringBuilder();

        buf.append("foreach ($v in get-clustercompatibilityversions -datacenterid ");
        buf.append(PowerShellUtils.escape(cluster.getDataCenter().getId()));
        buf.append(") { ");
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
        buf.append(" -datacenterid " + PowerShellUtils.escape(cluster.getDataCenter().getId()));
        buf.append(" -compatibilityversion $version");

        cluster = LinkHelper.addLinks(runAndParseSingle(buf.toString()));

        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(cluster.getId());

        return Response.created(uriBuilder.build()).entity(cluster).build();
    }

    @Override
    public void remove(String id) {
        PowerShellCmd.runCommand(getShell(), "remove-cluster -clusterid " + PowerShellUtils.escape(id));
        removeSubResource(id);
    }

    @Override
    public ClusterResource getClusterSubResource(UriInfo uriInfo, String id) {
        return getSubResource(id);
    }

    protected PowerShellClusterResource createSubResource(String id) {
        return new PowerShellClusterResource(id, getExecutor(), shellPools, getParser());
    }
}

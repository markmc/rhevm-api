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

import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.redhat.rhevm.api.resource.MediaType;

import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.Network;
import com.redhat.rhevm.api.model.Networks;
import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.powershell.model.PowerShellNetwork;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
import com.redhat.rhevm.api.resource.AssignedNetworkResource;
import com.redhat.rhevm.api.resource.AssignedNetworksResource;

import static com.redhat.rhevm.api.common.util.CompletenessAssertor.validateParameters;

@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class PowerShellClusterNetworksResource
    extends UriProviderWrapper
    implements AssignedNetworksResource {

    protected String clusterId;

    public PowerShellClusterNetworksResource(String clusterId,
                                             Executor executor,
                                             PowerShellPoolMap shellPools,
                                             PowerShellParser parser,
                                             UriInfoProvider uriProvider) {
        super(executor, shellPools, parser, uriProvider);
        this.clusterId = clusterId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public List<Network> runAndParse(String command) {
        return PowerShellNetwork.parse(getParser(), PowerShellCmd.runCommand(getPool(), command));
    }

    public Network runAndParseSingle(String command) {
        List<Network> networks = runAndParse(command);
        return !networks.isEmpty() ? networks.get(0) : null;
    }

    public Network addLinks(Network network) {
        network.setCluster(new Cluster());
        network.getCluster().setId(clusterId);

        return LinkHelper.addLinks(getUriInfo(), network);
    }

    private String getClusterLookup() {
        return "$cluster = select-cluster | ? { $_.clusterid -eq " + PowerShellUtils.escape(clusterId) + " }; ";
    }

    private String getNetworkLookup(String id) {
        return "$net = get-networks | ? { $_.networkid -eq " + PowerShellUtils.escape(id) + " }; ";
    }

    private String getNetworkByNameLookup(String name) {
        return "$net = get-networks | ? { $_.name -eq " + PowerShellUtils.escape(name) + " }; ";
    }

    public Network getClusterNetwork(String id) {
        StringBuilder buf = new StringBuilder();

        buf.append(getClusterLookup());
        buf.append("$cluster.getnetworks() | ? { $_.networkid -eq " + PowerShellUtils.escape(id) + " }");

        return runAndParseSingle(buf.toString());
    }

    @Override
    public Networks list() {
        StringBuilder buf = new StringBuilder();

        buf.append(getClusterLookup());
        buf.append("$cluster.getnetworks()");

        Networks ret = new Networks();
        for (Network network : runAndParse(buf.toString())) {
            ret.getNetworks().add(addLinks(network));
        }
        return ret;
    }

    @Override
    public Response add(Network network) {
        validateParameters(network, "id|name");

        StringBuilder buf = new StringBuilder();

        buf.append(getClusterLookup());

        if (network.isSetId()) {
            buf.append(getNetworkLookup(network.getId()));
        } else {
            buf.append(getNetworkByNameLookup(network.getName()));
        }

        buf.append("$cluster = add-networktocluster");
        buf.append(" -clusterobject $cluster");
        buf.append(" -networkobject $net; ");

        buf.append("$cluster.getnetworks() | ? { $_.networkid -eq $net.networkid }");

        network = addLinks(runAndParseSingle(buf.toString()));

        UriBuilder uriBuilder = getUriInfo().getAbsolutePathBuilder().path(network.getId());

        return Response.created(uriBuilder.build()).entity(network).build();
    }

    @Override
    public void remove(String id) {
        StringBuilder buf = new StringBuilder();

        buf.append(getClusterLookup());
        buf.append(getNetworkLookup(id));

        buf.append("$cluster = remove-networkfromcluster");
        buf.append(" -clusterobject $cluster");
        buf.append(" -networkobject $net");

        PowerShellCmd.runCommand(getPool(), buf.toString());
    }

    @Override
    public AssignedNetworkResource getAssignedNetworkSubResource(String id) {
        return new PowerShellClusterNetworkResource(id, this);
    }
}

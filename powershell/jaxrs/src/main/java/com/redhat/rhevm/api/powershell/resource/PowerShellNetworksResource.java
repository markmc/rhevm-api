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

import com.redhat.rhevm.api.model.IP;
import com.redhat.rhevm.api.model.Network;
import com.redhat.rhevm.api.model.Networks;
import com.redhat.rhevm.api.resource.NetworkResource;
import com.redhat.rhevm.api.resource.NetworksResource;
import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;


public class PowerShellNetworksResource
    extends AbstractPowerShellCollectionResource<Network, PowerShellNetworkResource>
    implements NetworksResource {

    public List<Network> runAndParse(String command) {
        return PowerShellNetworkResource.runAndParse(getShell(), getParser(), command);
    }

    public Network runAndParseSingle(String command) {
        return PowerShellNetworkResource.runAndParseSingle(getShell(), getParser(), command);
    }

    @Override
    public Networks list(UriInfo uriInfo) {
        Networks ret = new Networks();
        for (Network network : runAndParse("get-networks")) {
            ret.getNetworks().add(LinkHelper.addLinks(network));
        }
        return ret;
    }

    @Override
    public Response add(UriInfo uriInfo, Network network) {
        StringBuilder buf = new StringBuilder();

        buf.append("add-network");
        buf.append(" -name " + PowerShellUtils.escape(network.getName()) + "");
        buf.append(" -datacenterid " + PowerShellUtils.escape(network.getDataCenter().getId()));

        if (network.getDescription() != null) {
            buf.append(" -description " + PowerShellUtils.escape(network.getDescription()));
        }

        if (network.getIp() != null) {
            IP ip = network.getIp();

            if (ip.getAddress() != null) {
                buf.append(" -address " + PowerShellUtils.escape(ip.getAddress()));
            }
            if (ip.getNetmask() != null) {
                buf.append(" -netmask " + PowerShellUtils.escape(ip.getNetmask()));
            }
            if (ip.getGateway() != null) {
                buf.append(" -gateway " + PowerShellUtils.escape(ip.getGateway()));
            }
        }

        if (network.getVlan() != null) {
            buf.append(" -vlanid " + PowerShellUtils.escape(network.getVlan().getId()));
        }

        if (network.isStp() != null && network.isStp()) {
            buf.append(" -stp");
        }

        network = LinkHelper.addLinks(runAndParseSingle(buf.toString()));

        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(network.getId());

        return Response.created(uriBuilder.build()).entity(network).build();
    }

    @Override
    public void remove(String id) {
        StringBuilder buf = new StringBuilder();

        buf.append("$n = get-networks;");
        buf.append("foreach ($i in $n) {");
        buf.append("  if ($i.networkid -eq " + PowerShellUtils.escape(id) + ") {");
        buf.append("    remove-network");
        buf.append(" -networkobject $i");
        buf.append(" -datacenterid $i.datacenterid");
        buf.append("  }");
        buf.append("}");

        PowerShellCmd.runCommand(getShell(), buf.toString());

        removeSubResource(id);
    }

    @Override
    public NetworkResource getNetworkSubResource(UriInfo uriInfo, String id) {
        return getSubResource(id);
    }

    protected PowerShellNetworkResource createSubResource(String id) {
        return new PowerShellNetworkResource(id, getExecutor(), shellPools, getParser());
    }
}

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

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.NIC;
import com.redhat.rhevm.api.model.Nics;
import com.redhat.rhevm.api.model.Network;

import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.powershell.model.PowerShellVM;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;


public class PowerShellNicsResource extends AbstractPowerShellDevicesResource<NIC, Nics> {

    public PowerShellNicsResource(String vmId, PowerShellPoolMap powerShellPoolMap) {
        super(vmId, powerShellPoolMap);
    }

    public Nics runAndParse(String command) {
        return PowerShellVM.parseNics(vmId, PowerShellCmd.runCommand(getShell(), command));
    }

    public NIC runAndParseSingle(String command) {
        Nics nics = runAndParse(command);

        return (nics != null && !nics.getNics().isEmpty()) ? nics.getNics().get(0) : null;
    }

    @Override
    public Nics getDevices() {
        StringBuilder buf = new StringBuilder();

        buf.append("$v = get-vm " + PowerShellUtils.escape(vmId) + "\n");
        buf.append("$v.GetNetworkAdapters()\n");

        return runAndParse(buf.toString());
    }

    /* Map the network names to network ID on the supplied network
     * interfaces. The powershell output only includes the network name.
     *
     * @param nic  the NIC to modify
     * @return     the modified NIC
     */
    private NIC lookupNetworkId(NIC nic) {
        StringBuilder buf = new StringBuilder();

        buf.append("$n = get-networks\n");
        buf.append("foreach ($i in $n) {");
        buf.append("  if ($i.name -eq " + PowerShellUtils.escape(nic.getNetwork().getName()) + ") {");
        buf.append("    $i");
        buf.append("  }");
        buf.append("}");

        Network network = new Network();
        network.setId(PowerShellNetworkResource.runAndParseSingle(getShell(), buf.toString()).getId());
        nic.setNetwork(network);

        return nic;
    }

    @Override
    public NIC addLinks(NIC nic) {
        return LinkHelper.addLinks(lookupNetworkId(nic));
    }

    @Override
    public Nics list() {
        Nics nics = getDevices();
        for (NIC nic : nics.getNics()) {
            addLinks(nic);
        }
        return nics;
    }

    @Override
    public Response add(UriInfo uriInfo, NIC nic) {
        StringBuilder buf = new StringBuilder();

        buf.append("$v = get-vm " + PowerShellUtils.escape(vmId) + "\n");
        buf.append("foreach ($i in get-networks) {");
        buf.append("  if ($i.networkid -eq " + PowerShellUtils.escape(nic.getNetwork().getId()) + ") {");
        buf.append("    $n = $i");
        buf.append("  }");
        buf.append("}\n");

        buf.append("add-networkadapter");
        buf.append(" -vmobject $v");
        buf.append(" -interfacename " + PowerShellUtils.escape(nic.getName()));
        buf.append(" -networkname $n.name");
        if (nic.getType() != null) {
            buf.append(" -interfacetype " + nic.getType().toString().toLowerCase());
        }
        if (nic.getMac() != null && nic.getMac().getAddress() != null) {
            buf.append(" -macaddress " + PowerShellUtils.escape(nic.getMac().getAddress()));
        }

        nic = addLinks(runAndParseSingle(buf.toString()));

        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(nic.getId());

        return Response.created(uriBuilder.build()).entity(nic).build();
    }

    @Override
    public void remove(String id) {
        StringBuilder buf = new StringBuilder();

        buf.append("$v = get-vm " + PowerShellUtils.escape(vmId) + "\n");

        buf.append("foreach ($i in $v.GetNetworkAdapters()) {");
        buf.append("  if ($i.id -eq " + PowerShellUtils.escape(id) + ") {");
        buf.append("    $n = $i");
        buf.append("  }");
        buf.append("}\n");
        buf.append("remove-networkadapter -vmobject $v -networkadapterobject $n");

        PowerShellCmd.runCommand(getShell(), buf.toString());
    }

    @Override
    public PowerShellDeviceResource<NIC, Nics> getDeviceSubResource(String id) {
        return new PowerShellDeviceResource<NIC, Nics>(this, id, powerShellPoolMap);
    }
}

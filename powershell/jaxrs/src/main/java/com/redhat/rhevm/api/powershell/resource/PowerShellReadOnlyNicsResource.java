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

import com.redhat.rhevm.api.model.NIC;
import com.redhat.rhevm.api.model.Nics;
import com.redhat.rhevm.api.model.Network;
import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.powershell.model.PowerShellVM;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;


public class PowerShellReadOnlyNicsResource extends AbstractPowerShellDevicesResource<NIC, Nics> {

    private String getCommand;

    public PowerShellReadOnlyNicsResource(String parentId,
                                          PowerShellPoolMap shellPools,
                                          PowerShellParser parser,
                                          String getCommand) {
        super(parentId, shellPools, parser);
        this.getCommand = getCommand;
    }

    public Nics runAndParse(String command) {
        return PowerShellVM.parseNics(parentId, PowerShellCmd.runCommand(getShell(), command));
    }

    public NIC runAndParseSingle(String command) {
        Nics nics = runAndParse(command);

        return (nics != null && !nics.getNics().isEmpty()) ? nics.getNics().get(0) : null;
    }

    @Override
    public Nics getDevices() {
        StringBuilder buf = new StringBuilder();

        buf.append("$v = " + getCommand + " " + PowerShellUtils.escape(parentId) + "\n");
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
        network.setId(PowerShellNetworkResource.runAndParseSingle(getShell(), getParser(), buf.toString()).getId());
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
    public PowerShellDeviceResource<NIC, Nics> getDeviceSubResource(String id) {
        return new PowerShellDeviceResource<NIC, Nics>(this, id, shellPools);
    }
}

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

import com.redhat.rhevm.api.resource.MediaType;

import com.redhat.rhevm.api.model.HostNIC;
import com.redhat.rhevm.api.model.HostNics;
import com.redhat.rhevm.api.model.Network;
import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.powershell.model.PowerShellHostNIC;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPool;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
import com.redhat.rhevm.api.resource.HostNicResource;
import com.redhat.rhevm.api.resource.HostNicsResource;

@Produces(MediaType.APPLICATION_XML)
public class PowerShellHostNicsResource implements HostNicsResource {

    protected String hostId;
    protected Executor executor;
    protected PowerShellPoolMap shellPools;
    protected PowerShellParser parser;

    public PowerShellHostNicsResource(String hostId,
                                      Executor executor,
                                      PowerShellPoolMap shellPools,
                                      PowerShellParser parser) {
        this.hostId = hostId;
        this.executor = executor;
        this.shellPools = shellPools;
        this.parser = parser;
    }

    public Executor getExecutor() {
        return executor;
    }

    public PowerShellPool getPool() {
        return shellPools.get();
    }

    public PowerShellParser getParser() {
        return parser;
    }

    public String getHostId() {
        return hostId;
    }

    public List<HostNIC> runAndParse(String command) {
        return PowerShellHostNIC.parse(getParser(), hostId, PowerShellCmd.runCommand(getPool(), command));
    }

    public HostNIC runAndParseSingle(String command) {
        List<HostNIC> nics = runAndParse(command);
        return !nics.isEmpty() ? nics.get(0) : null;
    }

    /* Map the network names to network ID on the supplied host network
     * interfaces. The powershell output only includes the network name.
     *
     * @param nic  the host NIC to modify
     * @return     the modified host NIC
     */
    private HostNIC lookupNetworkId(HostNIC nic) {
        StringBuilder buf = new StringBuilder();

        buf.append("$n = get-networks; ");
        buf.append("foreach ($i in $n) { ");
        buf.append("if ($i.name -eq " + PowerShellUtils.escape(nic.getNetwork().getName()) + ") { ");
        buf.append("$i; break ");
        buf.append("} ");
        buf.append("}");

        Network network = new Network();
        network.setId(PowerShellNetworkResource.runAndParseSingle(getPool(), getParser(), buf.toString()).getId());
        nic.setNetwork(network);

        return nic;
    }

    public HostNIC addLinks(HostNIC nic) {
        return LinkHelper.addLinks(lookupNetworkId(nic));
    }

    public HostNIC getHostNic(String nicId) {
        StringBuilder buf = new StringBuilder();

        buf.append("$h = get-host " + PowerShellUtils.escape(hostId) + "; ");
        buf.append("foreach ($n in $h.getnetworkadapters()) { ");
        buf.append("if ($n.id -eq " + PowerShellUtils.escape(nicId) + ") { ");
        buf.append("$n; break ");
        buf.append("} ");
        buf.append("}");

        return runAndParseSingle(buf.toString());
    }

    @Override
    public HostNics list() {
        StringBuilder buf = new StringBuilder();

        buf.append("$h = get-host " + PowerShellUtils.escape(hostId) + "; ");
        buf.append("$h.getnetworkadapters()");

        HostNics ret = new HostNics();
        for (HostNIC nic : runAndParse(buf.toString())) {
            ret.getHostNics().add(addLinks(nic));
        }
        return ret;
    }

    @Override
    public HostNicResource getHostNicSubResource(String id) {
        return new PowerShellHostNicResource(id, getExecutor(), shellPools, getParser(), this);
    }
}

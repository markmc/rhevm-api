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
package com.redhat.rhevm.api.command.networks;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;

import com.redhat.rhevm.api.command.base.AbstractAddCommand;
import com.redhat.rhevm.api.model.DataCenter;
import com.redhat.rhevm.api.model.IP;
import com.redhat.rhevm.api.model.Network;
import com.redhat.rhevm.api.model.VLAN;

/**
 * Add a new Network.
 */
@Command(scope = "network", name = "add", description = "Add a new Network")
public class NetworksAddCommand extends AbstractAddCommand<Network> {

    @Argument(index = 0, name = "name", description = "Name of the Network to add", required = true, multiValued = false)
    protected String name;

    @Option(name = "-d", aliases = { "--datacenter" }, description = "Data center ID", required = true, multiValued = false)
    private String datacenter;


    @Option(name = "-a", aliases = { "--ip-address" }, description = "IP address", required = false, multiValued = false)
    private String address;

    @Option(name = "-n", aliases = { "--ip-netmask" }, description = "IP netmask", required = false, multiValued = false)
    private String netmask;

    @Option(name = "-g", aliases = { "--ip-gateway" }, description = "IP gateway", required = false, multiValued = false)
    private String gateway;

    @Option(name = "-v", aliases = { "--vlan-id" }, description = "VLAN ID", required = false, multiValued = false)
    private int vlan;

    @Option(name = "-s", aliases = { "--stp" }, description = "STP", required = false, multiValued = false)
    private boolean stp;

    protected Object doExecute() throws Exception {
        display(doAdd(getModel(), Network.class, "networks", "network"));
        return null;
    }

    protected Network getModel() {
        Network model = new Network();
        model.setName(name);
        model.setDataCenter(new DataCenter());
        model.getDataCenter().setId(datacenter);
        if (address != null || netmask != null || gateway != null) {
            model.setIp(new IP());
            model.getIp().setAddress(address);
            model.getIp().setNetmask(netmask);
            model.getIp().setGateway(gateway);
        }
        if (vlan != -1) {
            model.setVlan(new VLAN());
            model.getVlan().setId(vlan);
        }
        model.setStp(stp);
        return model;
    }
}

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
package com.redhat.rhevm.api.powershell.model;

import java.util.ArrayList;
import java.util.HashMap;

import com.redhat.rhevm.api.model.DataCenter;
import com.redhat.rhevm.api.model.IP;
import com.redhat.rhevm.api.model.Network;
import com.redhat.rhevm.api.model.NetworkStatus;
import com.redhat.rhevm.api.powershell.model.PowerShellNetwork;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;


public class PowerShellNetwork {

    public static ArrayList<Network> parse(String output) {
        ArrayList<HashMap<String,String>> networksProps = PowerShellUtils.parseProps(output);
        ArrayList<Network> ret = new ArrayList<Network>();

        for (HashMap<String,String> props : networksProps) {
            Network network = new Network();

            network.setId(props.get("networkid"));
            network.setName(props.get("name"));
            network.setDescription(props.get("description"));

            DataCenter dataCenter = new DataCenter();
            dataCenter.setId(props.get("datacenterid"));
            network.setDataCenter(dataCenter);

            if (props.get("address") != null ||
                props.get("subnet") != null ||
                props.get("gateway") != null) {
                IP ip = new IP();
                ip.setAddress(props.get("address"));
                ip.setNetmask(props.get("subnet"));
                ip.setGateway(props.get("gateway"));
                network.setIp(ip);
            }

            if (props.get("vlanid") != null) {
                Network.Vlan vlan = new Network.Vlan();
                vlan.setId(props.get("vlanid"));
                network.setVlan(vlan);
            }

            if (props.get("stp") != null &&
                props.get("stp").toLowerCase().equals("true")) {
                network.setStp(true);
            }

            if (props.get("status") != null) {
                if (props.get("status").toLowerCase().equals("operational")) {
                    network.setStatus(NetworkStatus.OPERATIONAL);
                } else {
                    network.setStatus(NetworkStatus.NON_OPERATIONAL);
                }
            }

            ret.add(network);
        }

        return ret;
    }
}

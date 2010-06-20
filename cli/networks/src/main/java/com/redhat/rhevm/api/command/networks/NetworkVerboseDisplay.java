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

import com.redhat.rhevm.api.model.Network;

import com.redhat.rhevm.api.command.base.VerboseDisplay;

public class NetworkVerboseDisplay implements VerboseDisplay<Network> {
    @Override
    public void expand(Network model) {
        if (model.isSetStatus()) {
            System.out.println("  Status: " + model.getStatus());
        }
        if (model.isSetDataCenter() && model.getDataCenter().isSetName()) {
            System.out.println("  data center: " + model.getDataCenter().getName());
        }
        if (model.isSetIp()) {
            if (model.getIp().isSetAddress()) {
               System.out.println("  IP address: " + model.getIp().getAddress());
            }
            if (model.getIp().isSetNetmask()) {
               System.out.println("  IP netmask: " + model.getIp().getNetmask());
            }
            if (model.getIp().isSetGateway()) {
               System.out.println("  IP gateway: " + model.getIp().getGateway());
            }
        }
        if (model.isSetVlan()) {
            System.out.println("  VLAN ID: " + model.getVlan().getId());
        }
        if (model.isSetStp()) {
            System.out.println("  STP: " + model.isStp());
        }
    }
}

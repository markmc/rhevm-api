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

import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.Devices;
import com.redhat.rhevm.api.model.Disk;
import com.redhat.rhevm.api.model.DiskFormat;
import com.redhat.rhevm.api.model.DiskInterface;
import com.redhat.rhevm.api.model.DiskStatus;
import com.redhat.rhevm.api.model.DiskType;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.powershell.model.PowerShellVM;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;

public class PowerShellVM {

    public static ArrayList<VM> parse(String output) {
        ArrayList<HashMap<String,String>> vmsProps = PowerShellUtils.parseProps(output);
        ArrayList<VM> ret = new ArrayList<VM>();

        for (HashMap<String,String> props : vmsProps) {
            VM vm = new VM();

            vm.setId(props.get("vmid"));
            vm.setName(props.get("name"));
            vm.setDescription(props.get("description"));

            Cluster cluster = new Cluster();
            cluster.setId(props.get("hostclusterid"));
            vm.setCluster(cluster);

            ret.add(vm);
        }

        return ret;
    }

    public static VM parseDisks(VM vm, String output) {
        ArrayList<HashMap<String,String>> diskProps = PowerShellUtils.parseProps(output);

        for (HashMap<String,String> props : diskProps) {
            Disk disk = new Disk();

            disk.setSize(Integer.parseInt(props.get("actualsizeinbytes")));
            disk.setType(DiskType.fromValue(props.get("disktype").toUpperCase()));
            disk.setStatus(DiskStatus.fromValue(props.get("status").toUpperCase()));
            disk.setInterface(DiskInterface.fromValue(props.get("diskinterface").toUpperCase()));
            disk.setFormat(DiskFormat.fromValue(props.get("volumeformat").toUpperCase()));
            if (props.get("volumetype").toLowerCase() == "sparse") {
                disk.setSparse(true);
            }
            if (props.get("boot").toLowerCase() == "true") {
                disk.setBootable(true);
            }
            if (props.get("wipeafterdelete").toLowerCase() == "true") {
                disk.setWipeAfterDelete(true);
            }
            if (props.get("propagateerrors").toLowerCase() == "on") {
                disk.setPropagateErrors(true);
            }

            if (vm.getDevices() == null) {
                vm.setDevices(new Devices());
            }

            vm.getDevices().getDisks().add(disk);
        }


        return vm;
    }
}

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

import com.redhat.rhevm.api.model.BootDevice;
import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.CPU;
import com.redhat.rhevm.api.model.CpuTopology;
import com.redhat.rhevm.api.model.Devices;
import com.redhat.rhevm.api.model.Disk;
import com.redhat.rhevm.api.model.DiskFormat;
import com.redhat.rhevm.api.model.DiskInterface;
import com.redhat.rhevm.api.model.DiskStatus;
import com.redhat.rhevm.api.model.DiskType;
import com.redhat.rhevm.api.model.NIC;
import com.redhat.rhevm.api.model.NicType;
import com.redhat.rhevm.api.model.IP;
import com.redhat.rhevm.api.model.Network;
import com.redhat.rhevm.api.model.OperatingSystem;
import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.model.VmPool;
import com.redhat.rhevm.api.model.VmStatus;
import com.redhat.rhevm.api.powershell.model.PowerShellVM;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;

public class PowerShellVM extends VM {

    private String cdIsoPath;
    public String getCdIsoPath() {
        return cdIsoPath;
    }
    public void setCdIsoPath(String cdIsoPath) {
        this.cdIsoPath = cdIsoPath;
    }

    public static String buildBootSequence(VM vm) {
        if (vm.getOs() == null || vm.getOs().getBoot().size() <= 0) {
            return null;
        }
        String bootSequence = "";
        for (OperatingSystem.Boot boot : vm.getOs().getBoot()) {
            if (boot.getDev() == null) {
                continue;
            }
            switch (boot.getDev()) {
            case HD:
                bootSequence += "C";
                break;
            case CDROM:
                bootSequence += "D";
                break;
            case NETWORK:
                bootSequence += "N";
                break;
            default:
                break;
            }
        }
        return !bootSequence.isEmpty() ? bootSequence : null;
    }

    private static void parseBootDevices(PowerShellVM vm, String bootSequence) {
        for (int i = 0; i < bootSequence.length(); i++) {
            char c = bootSequence.charAt(i);
            OperatingSystem.Boot boot = new OperatingSystem.Boot();

            switch (c) {
            case 'C':
                boot.setDev(BootDevice.HD);
                break;
            case 'D':
                boot.setDev(BootDevice.CDROM);
                break;
            case 'N':
                boot.setDev(BootDevice.NETWORK);
                break;
            default:
                break;
            }

            if (boot.isSetDev()) {
                vm.getOs().getBoot().add(boot);
            }
        }
    }

    private static VmStatus parseStatus(HashMap<String,String> props, String key) {
        String s = props.get(key);
        if (s == null) return null;
        else if (s.equals("Down"))         return VmStatus.SHUTOFF;
        else if (s.equals("Paused"))       return VmStatus.PAUSED;
        else if (s.equals("PoweringDown")) return VmStatus.SHUTDOWN;
        else if (s.equals("Up"))           return VmStatus.RUNNING;
        else return null;
    }

    public static ArrayList<PowerShellVM> parse(String output) {
        ArrayList<HashMap<String,String>> vmsProps = PowerShellUtils.parseProps(output);
        ArrayList<PowerShellVM> ret = new ArrayList<PowerShellVM>();

        for (HashMap<String,String> props : vmsProps) {
            PowerShellVM vm = new PowerShellVM();

            vm.setId(props.get("vmid"));
            vm.setName(props.get("name"));
            vm.setDescription(props.get("description"));
            vm.setMemory(Long.parseLong(props.get("memorysize")) * 1024 * 1024);
            vm.setCdIsoPath(props.get("cdisopath"));

            VmStatus status = parseStatus(props, "status");
            if (status != null) {
                vm.setStatus(status);
            }

            CpuTopology topo = new CpuTopology();
            topo.setSockets(Integer.parseInt(props.get("numofsockets")));
            topo.setCores(Integer.parseInt(props.get("numofcpuspersocket")));
            CPU cpu = new CPU();
            cpu.setTopology(topo);
            vm.setCpu(cpu);

            OperatingSystem os = new OperatingSystem();
            vm.setOs(os);
            parseBootDevices(vm, props.get("defaultbootsequence"));

            Cluster cluster = new Cluster();
            cluster.setId(props.get("hostclusterid"));
            vm.setCluster(cluster);

            Template template = new Template();
            template.setId(props.get("templateid"));
            vm.setTemplate(template);

            if (!props.get("poolid").equals("-1")) {
                VmPool pool = new VmPool();
                pool.setId(props.get("poolid"));
                vm.setVmPool(pool);
            }

            ret.add(vm);
        }

        return ret;
    }

    public static PowerShellVM parseDisks(PowerShellVM vm, String output) {
        ArrayList<HashMap<String,String>> diskProps = PowerShellUtils.parseProps(output);

        if (vm.getDevices() == null) {
            vm.setDevices(new Devices());
        }

        for (HashMap<String,String> props : diskProps) {
            Disk disk = new Disk();

            disk.setId(props.get("snapshotid"));
            disk.setSize(Long.parseLong(props.get("actualsizeinbytes")));
            disk.setType(DiskType.fromValue(props.get("disktype").toUpperCase()));
            disk.setStatus(DiskStatus.fromValue(props.get("status").toUpperCase()));
            disk.setInterface(DiskInterface.fromValue(props.get("diskinterface").toUpperCase()));
            disk.setFormat(DiskFormat.fromValue(props.get("volumeformat").toUpperCase()));
            if (props.get("volumetype").toLowerCase().equals("sparse")) {
                disk.setSparse(true);
            }
            if (props.get("boot").toLowerCase().equals("true")) {
                disk.setBootable(true);
            }
            if (props.get("wipeafterdelete").toLowerCase().equals("true")) {
                disk.setWipeAfterDelete(true);
            }
            if (props.get("propagateerrors").toLowerCase().equals("on")) {
                disk.setPropagateErrors(true);
            }

            vm.getDevices().getDisks().add(disk);
        }


        return vm;
    }

    public static PowerShellVM parseNics(PowerShellVM vm, String output) {
        ArrayList<HashMap<String,String>> ifaceProps = PowerShellUtils.parseProps(output);

        if (vm.getDevices() == null) {
            vm.setDevices(new Devices());
        }

        for (HashMap<String,String> props : ifaceProps) {
            NIC nic = new NIC();

            nic.setId(props.get("id"));
            nic.setName(props.get("name"));

            Network network = new Network();
            network.setName(props.get("network"));
            nic.setNetwork(network);

            nic.setType(NicType.fromValue(props.get("type").toUpperCase()));

            if (props.get("macaddress") != null) {
                NIC.Mac mac = new NIC.Mac();
                mac.setAddress(props.get("macaddress"));
                nic.setMac(mac);
            }

            if (props.get("address") != null ||
                props.get("subnet") != null ||
                props.get("gateway") != null) {
                IP ip = new IP();
                ip.setAddress(props.get("address"));
                ip.setNetmask(props.get("subnet"));
                ip.setGateway(props.get("gateway"));
                nic.setIp(ip);
            }

            vm.getDevices().getNics().add(nic);
        }

        return vm;
    }
}

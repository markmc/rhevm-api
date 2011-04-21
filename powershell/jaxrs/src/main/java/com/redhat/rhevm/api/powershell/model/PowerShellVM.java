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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import com.redhat.rhevm.api.common.util.TimeZoneMapping;
import com.redhat.rhevm.api.model.BootDevice;
import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.CPU;
import com.redhat.rhevm.api.model.CpuTopology;
import com.redhat.rhevm.api.model.Disk;
import com.redhat.rhevm.api.model.Disks;
import com.redhat.rhevm.api.model.Domain;
import com.redhat.rhevm.api.model.NIC;
import com.redhat.rhevm.api.model.Nics;
import com.redhat.rhevm.api.model.Tag;
import com.redhat.rhevm.api.model.Tags;
import com.redhat.rhevm.api.model.Display;
import com.redhat.rhevm.api.model.DisplayType;
import com.redhat.rhevm.api.model.GuestInfo;
import com.redhat.rhevm.api.model.HighAvailability;
import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.model.IP;
import com.redhat.rhevm.api.model.OperatingSystem;
import com.redhat.rhevm.api.model.Boot;
import com.redhat.rhevm.api.model.Statistic;
import com.redhat.rhevm.api.model.Statistics;
import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.model.VmPlacementPolicy;
import com.redhat.rhevm.api.model.VmPool;
import com.redhat.rhevm.api.model.VmStatus;
import com.redhat.rhevm.api.powershell.enums.PowerShellBootSequence;
import com.redhat.rhevm.api.powershell.enums.PowerShellDisplayType;
import com.redhat.rhevm.api.powershell.enums.PowerShellVmType;
import com.redhat.rhevm.api.powershell.resource.PowerShellVmsResource.Detail;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
import com.redhat.rhevm.api.powershell.util.UUID;

import static com.redhat.rhevm.api.powershell.util.PowerShellUtils.last;

public class PowerShellVM extends VM {

    private static final String VM_TYPE = "RhevmCmd.CLIVm";
    private static final String NIC_TYPE = "RhevmCmd.CLIHostNetworkAdapter";

    private String cdIsoPath;
    public String getCdIsoPath() {
        return cdIsoPath;
    }
    public void setCdIsoPath(String cdIsoPath) {
        this.cdIsoPath = cdIsoPath;
    }

    private String taskIds;

    public String getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(String taskIds) {
        this.taskIds = taskIds;
    }

    public static String buildBootSequence(OperatingSystem os) {
        if (os == null || os.getBoot().size() <= 0) {
            return null;
        }
        String bootSequence = "";
        for (Boot boot : os.getBoot()) {
            BootDevice dev = boot.getDev() != null ? BootDevice.fromValue(boot.getDev()) : null;
            if (dev == null) {
                continue;
            }
            switch (dev) {
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

    public static String asString(DisplayType type) {
        return PowerShellDisplayType.forModel(type).name();
    }

    public static DisplayType parseDisplayType(String s) {
        return PowerShellDisplayType.valueOf(s).map();
    }

    private static VmStatus parseStatus(String s) {
        if (s == null) return null;
        else if (s.equals("Unassigned"))         return VmStatus.UNASSIGNED;
        else if (s.equals("Down"))               return VmStatus.DOWN;
        else if (s.equals("Up"))                 return VmStatus.UP;
        else if (s.equals("Powering Up"))        return VmStatus.POWERING_UP;
        else if (s.equals("Powered Down"))       return VmStatus.POWERED_DOWN;
        else if (s.equals("Paused"))             return VmStatus.PAUSED;
        else if (s.equals("Migrating From"))     return VmStatus.MIGRATING_FROM;
        else if (s.equals("Migrating To"))       return VmStatus.MIGRATING_TO;
        else if (s.equals("Unknown"))            return VmStatus.UNKNOWN;
        else if (s.equals("Not Responding"))     return VmStatus.NOT_RESPONDING;
        else if (s.equals("Wait For Launch"))    return VmStatus.WAIT_FOR_LAUNCH;
        else if (s.equals("Reboot In Progress")) return VmStatus.REBOOT_IN_PROGRESS;
        else if (s.equals("Saving State"))       return VmStatus.SAVING_STATE;
        else if (s.equals("Restoring State"))    return VmStatus.RESTORING_STATE;
        else if (s.equals("Suspended"))          return VmStatus.SUSPENDED;
        else if (s.equals("Image Illegal"))      return VmStatus.IMAGE_ILLEGAL;
        else if (s.equals("Image Locked"))       return VmStatus.IMAGE_LOCKED;
        else if (s.equals("Powering Down"))      return VmStatus.POWERING_DOWN;
        else return null;
    }

    private static String parseOrigin(String s) {
        return s != null ? s.toLowerCase() : null;
    }

    public static List<PowerShellVM> parse(PowerShellParser parser, String output, Set<Detail> details) {
        List<PowerShellVM> ret = new ArrayList<PowerShellVM>();

        Map<String, XMLGregorianCalendar> dates = new HashMap<String, XMLGregorianCalendar>();
        String date = null;

        String storageDomainId = null;

        for (PowerShellParser.Entity entity : parser.parse(output)) {
            if (PowerShellParser.DATE_TYPE.equals(entity.getType())) {
                date = entity.getValue();
            } else if (PowerShellParser.STRING_TYPE.equals(entity.getType())) {
                dates.put(date, PowerShellUtils.parseDate(entity.getValue()));
                date = null;
            } else if (PowerShellStorageDomain.isStorageDomain(entity)) {
                storageDomainId = PowerShellStorageDomain.parseEntity(entity).getId();
            } else if (PowerShellAsyncTask.isTask(entity)) {
                last(ret).setTaskIds(PowerShellAsyncTask.parseTask(entity, last(ret).getTaskIds()));
            } else if (PowerShellAsyncTask.isStatus(entity)) {
                last(ret).setCreationStatus(PowerShellAsyncTask.parseStatus(entity, last(ret).getCreationStatus()));
            } else if (hasDetail(details, Detail.STATISTICS) && PowerShellVmStatisticsParser.isMemory(entity)) {
                getStatistics(ret).addAll(PowerShellVmStatisticsParser.parseMemoryStats(entity));
            } else if (hasDetail(details, Detail.STATISTICS) && PowerShellVmStatisticsParser.isCpu(entity)) {
                getStatistics(ret).addAll(PowerShellVmStatisticsParser.parseCpuStats(entity));
            } else if (hasDetail(details, Detail.DISKS) && PowerShellDisk.isDisk(entity)) {
                getDisks(ret).add(PowerShellDisk.parseEntity(last(ret).getId(), entity, storageDomainId));
            } else if (hasDetail(details, Detail.NICS) && PowerShellNIC.isNIC(entity)) {
                getNics(ret).add(PowerShellNIC.parseEntity(last(ret).getId(), entity));
            } else if (hasDetail(details, Detail.TAGS) && PowerShellTag.isTag(entity)) {
                getTags(ret).add(PowerShellTag.parseEntity(entity));
            } else if (VM_TYPE.equals(entity.getType())) {
                ret.add(parseVm(entity, dates));
                if (hasDetail(details, Detail.STATISTICS)) {
                    last(ret).setStatistics(new Statistics());
                }
                if (hasDetail(details, Detail.DISKS)) {
                    last(ret).setDisks(new Disks());
                }
                if (hasDetail(details, Detail.NICS)) {
                    last(ret).setNics(new Nics());
                }
                if (hasDetail(details, Detail.TAGS)) {
                    last(ret).setTags(new Tags());
                }
            } else if (NIC_TYPE.equals(entity.getType())) {
                parseDisplayAddress(entity, last(ret));
            }
        }

        return ret;
    }

    private static PowerShellVM parseVm(PowerShellParser.Entity entity, Map<String, XMLGregorianCalendar> dates) {
        PowerShellVM vm = new PowerShellVM();

        vm.setId(entity.get("vmid"));
        vm.setName(entity.get("name"));
        vm.setDescription(entity.get("description"));
        vm.setType(entity.get("vmtype", PowerShellVmType.class).map().value());
        vm.setMemory(entity.get("memorysize", Integer.class) * 1024L * 1024L);
        vm.setCdIsoPath(entity.get("cdisopath"));

        VmStatus status = parseStatus(entity.get("status"));
        if (status != null) {
            vm.setStatus(status);
        }

        Integer defaultHostId = entity.get("defaulthostid", Integer.class);
        if (defaultHostId != null) {
            vm.setPlacementPolicy(new VmPlacementPolicy());
            vm.getPlacementPolicy().setHost(new Host());
            vm.getPlacementPolicy().getHost().setId(defaultHostId.toString());
        }

        CpuTopology topo = new CpuTopology();
        topo.setSockets(entity.get("numofsockets", Integer.class));
        topo.setCores(entity.get("numofcpuspersocket", Integer.class));
        CPU cpu = new CPU();
        cpu.setTopology(topo);
        vm.setCpu(cpu);

        OperatingSystem os = new OperatingSystem();
        os.setType(entity.get("operatingsystem"));
        for (Boot boot : entity.get("defaultbootsequence", PowerShellBootSequence.class).map()) {
            os.getBoot().add(boot);
        }
        vm.setOs(os);

        vm.setStateless(entity.get("stateless", Boolean.class));
        vm.setTimezone(TimeZoneMapping.getJava(entity.get("timezone")));

        String domain = entity.get("domain");
        if (domain != null) {
            vm.setDomain(new Domain());
            vm.getDomain().setName(domain);
        }

        vm.setHighAvailability(new HighAvailability());
        vm.getHighAvailability().setEnabled(entity.get("highlyavailable", Boolean.class));
        vm.getHighAvailability().setPriority(entity.get("priority", Integer.class));

        String ip = entity.get("ip");
        if (ip != null) {
            vm.setGuestInfo(new GuestInfo());
            vm.getGuestInfo().setIp(new IP());
            vm.getGuestInfo().getIp().setAddress(ip);
        }

        Object hostId = entity.get("runningonhost", String.class, Integer.class);
        if (!isEmptyId(hostId)) {
            Host host = new Host();
            host.setId(hostId.toString());
            vm.setHost(host);
        }

        Cluster cluster = new Cluster();
        cluster.setId(entity.get("hostclusterid", String.class, Integer.class).toString());
        vm.setCluster(cluster);

        Template template = new Template();
        template.setId(entity.get("templateid"));
        vm.setTemplate(template);

        Object poolId = entity.get("poolid", String.class, Integer.class);
        if (!isEmptyId(poolId)) {
            VmPool pool = new VmPool();
            pool.setId(poolId.toString());
            vm.setVmPool(pool);
        }

        DisplayType displayType = parseDisplayType(entity.get("displaytype"));
        if (displayType != null) {
            Display display = new Display();
            display.setType(displayType.value());
            display.setMonitors(entity.get("numofmonitors", Integer.class));
            int port = entity.get("displayport", Integer.class);
            if (port != -1) {
                display.setPort(port);
            }
            vm.setDisplay(display);
        }

        vm.setStartTime(PowerShellUtils.getDate(entity.get("elapsedtime", BigDecimal.class).intValue()));

        if (dates.containsKey(entity.get("creationdate"))) {
            vm.setCreationTime(dates.get(entity.get("creationdate")));
        }

        vm.setOrigin(parseOrigin(entity.get("origin")));

        return vm;
    }

    private static void parseDisplayAddress(PowerShellParser.Entity entity, PowerShellVM vm) {
        String displayAddress = entity.get("address");
        if (displayAddress != null) {
            if (!vm.isSetDisplay()) {
                vm.setDisplay(new Display());
            }
            vm.getDisplay().setAddress(displayAddress);
        }
    }

    private static boolean isEmptyId(Object id) {
        return id instanceof String && id.equals(UUID.EMPTY)
               || id instanceof Integer && id.equals(-1);
    }

    private static boolean hasDetail(Set<Detail> details, Detail detail) {
        return details != null && details.contains(detail);
    }

    private static List<Statistic> getStatistics(List<PowerShellVM> vms) {
        return last(vms).getStatistics().getStatistics();
    }

    private static List<Disk> getDisks(List<PowerShellVM> vms) {
        return last(vms).getDisks().getDisks();
    }

    private static List<NIC> getNics(List<PowerShellVM> vms) {
        return last(vms).getNics().getNics();
    }

    private static List<Tag> getTags(List<PowerShellVM> vms) {
        return last(vms).getTags().getTags();
    }
}

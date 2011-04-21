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
import java.util.List;
import java.util.Map;
import javax.xml.datatype.XMLGregorianCalendar;

import com.redhat.rhevm.api.model.Disk;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.powershell.enums.PowerShellDiskInterface;
import com.redhat.rhevm.api.powershell.enums.PowerShellDiskType;
import com.redhat.rhevm.api.powershell.enums.PowerShellImageStatus;
import com.redhat.rhevm.api.powershell.enums.PowerShellPropagateErrors;
import com.redhat.rhevm.api.powershell.enums.PowerShellVolumeFormat;
import com.redhat.rhevm.api.powershell.enums.PowerShellVolumeType;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;

import static com.redhat.rhevm.api.powershell.util.PowerShellUtils.last;

public class PowerShellDisk extends Disk {

    private static final String DISKS_TYPE = "RhevmCmd.CLIImage";

    private String vmSnapshotId;
    private String parentId;
    private XMLGregorianCalendar lastModified;
    private String internalDriveMapping;
    private String taskIds;

    static boolean isDisk(PowerShellParser.Entity entity) {
        return DISKS_TYPE.equals(entity.getType());
    }

    public String getVmSnapshotId() {
        return vmSnapshotId;
    }
    public void setVmSnapshotId(String vmSnapshotId) {
        this.vmSnapshotId = vmSnapshotId;
    }

    public String getParentId() {
        return parentId;
    }
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public XMLGregorianCalendar getLastModified() {
        return lastModified;
    }
    public void setLastModified(XMLGregorianCalendar lastModified) {
        this.lastModified = lastModified;
    }

    public String getInternalDriveMapping() {
        return internalDriveMapping;
    }
    public void setInternalDriveMapping(String internalDriveMapping) {
        this.internalDriveMapping = internalDriveMapping;
    }

    public String getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(String taskIds) {
        this.taskIds = taskIds;
    }

    public static List<PowerShellDisk> parse(PowerShellParser parser, String vmId, String output) {
        List<PowerShellDisk> ret = new ArrayList<PowerShellDisk>();

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
            } else if (isDisk(entity)) {
                ret.add(parseEntity(vmId, entity, storageDomainId, dates));
            }
        }

        return ret;
    }

    public static PowerShellDisk parseEntity(String vmId, PowerShellParser.Entity entity, String storageDomainId){
        return parseEntity(vmId, entity, storageDomainId, null);
    }

    private static PowerShellDisk parseEntity(String vmId,
                                              PowerShellParser.Entity entity,
                                              String storageDomainId,
                                              Map<String, XMLGregorianCalendar> dates) {
        PowerShellDisk disk = new PowerShellDisk();

        disk.setId(entity.get("snapshotid"));
        disk.setDescription(entity.get("description"));

        disk.setVm(new VM());
        disk.getVm().setId(vmId);

        if (storageDomainId != null) {
            disk.setStorageDomain(new StorageDomain());
            disk.getStorageDomain().setId(storageDomainId);
        }

        disk.setSize(entity.get("actualsizeinbytes", Double.class).longValue());
        disk.setType(entity.get("disktype", PowerShellDiskType.class).map().value());
        disk.setStatus(entity.get("status", PowerShellImageStatus.class).map());
        disk.setInterface(PowerShellDiskInterface.valueOf(entity.get("diskinterface")).map().value());
        disk.setFormat(entity.get("volumeformat", PowerShellVolumeFormat.class).map().value());
        disk.setSparse(entity.get("volumetype", PowerShellVolumeType.class).map());
        disk.setBootable(entity.get("boot", Boolean.class));
        disk.setWipeAfterDelete(entity.get("wipeafterdelete", Boolean.class));
        disk.setPropagateErrors(entity.get("propagateerrors", PowerShellPropagateErrors.class).map());

        disk.setVmSnapshotId(entity.get("vmsnapshotid"));
        disk.setParentId(entity.get("parentid"));
        disk.setInternalDriveMapping(entity.get("internaldrivemapping"));

        if (dates != null && dates.containsKey(entity.get("lastmodified"))) {
            disk.setLastModified(dates.get(entity.get("lastmodified")));
        }

        return disk;
    }
}

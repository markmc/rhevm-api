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
import java.util.List;

import com.redhat.rhevm.api.model.Disk;
import com.redhat.rhevm.api.model.DiskFormat;
import com.redhat.rhevm.api.model.DiskInterface;
import com.redhat.rhevm.api.model.DiskStatus;
import com.redhat.rhevm.api.model.DiskType;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.powershell.enums.PowerShellDiskType;
import com.redhat.rhevm.api.powershell.enums.PowerShellImageStatus;
import com.redhat.rhevm.api.powershell.enums.PowerShellPropagateErrors;
import com.redhat.rhevm.api.powershell.enums.PowerShellVolumeFormat;
import com.redhat.rhevm.api.powershell.enums.PowerShellVolumeFormat22;
import com.redhat.rhevm.api.powershell.enums.PowerShellVolumeType;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;

public class PowerShellDisk extends Disk {

    private String vmSnapshotId;

    public String getVmSnapshotId() {
        return vmSnapshotId;
    }
    public void setVmSnapshotId(String vmSnapshotId) {
        this.vmSnapshotId = vmSnapshotId;
    }

    public static List<PowerShellDisk> parse(PowerShellParser parser, String vmId, String output) {
        List<PowerShellDisk> ret = new ArrayList<PowerShellDisk>();

        for (PowerShellParser.Entity entity : parser.parse(output)) {
            PowerShellDisk disk = new PowerShellDisk();

            disk.setId(entity.get("snapshotid"));

            disk.setVm(new VM());
            disk.getVm().setId(vmId);

            disk.setSize(entity.get("actualsizeinbytes", Double.class).longValue());
            disk.setType(entity.get("disktype", PowerShellDiskType.class).map());
            disk.setStatus(entity.get("status", PowerShellImageStatus.class).map());
            disk.setInterface(DiskInterface.fromValue(entity.get("diskinterface").toUpperCase()));
            try {
                disk.setFormat(entity.get("volumeformat", PowerShellVolumeFormat.class).map());
            } catch (ClassCastException ex) {
                disk.setFormat(entity.get("volumeformat", PowerShellVolumeFormat22.class).map());
            }
            if (entity.get("volumetype", PowerShellVolumeType.class).map()) {
                disk.setSparse(true);
            }
            if (entity.get("boot", Boolean.class)) {
                disk.setBootable(true);
            }
            if (entity.get("wipeafterdelete", Boolean.class)) {
                disk.setWipeAfterDelete(true);
            }
            if (entity.get("propagateerrors", PowerShellPropagateErrors.class).map()) {
                disk.setPropagateErrors(true);
            }

            disk.setVmSnapshotId(entity.get("vmsnapshotid"));

            ret.add(disk);
        }

        return ret;
    }
}

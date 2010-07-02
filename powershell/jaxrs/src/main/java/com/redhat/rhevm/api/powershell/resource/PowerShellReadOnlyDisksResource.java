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

import com.redhat.rhevm.api.model.Disk;
import com.redhat.rhevm.api.model.Disks;

import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.powershell.model.PowerShellVM;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;


public class PowerShellReadOnlyDisksResource extends AbstractPowerShellDevicesResource<Disk, Disks> {

    public PowerShellReadOnlyDisksResource(String parentId, PowerShellPoolMap shellPools) {
        super(parentId, shellPools);
    }

    public Disks runAndParse(String command) {
        return PowerShellVM.parseDisks(parentId, PowerShellCmd.runCommand(getShell(), command));
    }

    public Disk runAndParseSingle(String command) {
        Disks disks = runAndParse(command);

        return (disks != null && !disks.getDisks().isEmpty()) ? disks.getDisks().get(0) : null;
    }

    @Override
    public Disks getDevices() {
        StringBuilder buf = new StringBuilder();

        buf.append("$v = get-vm " + PowerShellUtils.escape(parentId) + "\n");
        buf.append("$v.GetDiskImages()\n");

        return runAndParse(buf.toString());
    }

    @Override
    public Disk addLinks(Disk disk) {
        return LinkHelper.addLinks(disk);
    }

    @Override
    public Disks list() {
        Disks disks = getDevices();
        for (Disk disk : disks.getDisks()) {
            addLinks(disk);
        }
        return disks;
    }

    @Override
    public PowerShellDeviceResource<Disk, Disks> getDeviceSubResource(String id) {
        return new PowerShellDeviceResource<Disk, Disks>(this, id, shellPools);
    }
}

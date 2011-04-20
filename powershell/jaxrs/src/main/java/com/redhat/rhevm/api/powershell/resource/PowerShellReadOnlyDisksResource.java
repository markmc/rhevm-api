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

import java.util.ArrayList;
import java.util.List;

import com.redhat.rhevm.api.model.Disk;
import com.redhat.rhevm.api.model.Disks;

import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.common.util.JAXBHelper;
import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.powershell.model.PowerShellDisk;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;


public class PowerShellReadOnlyDisksResource extends AbstractPowerShellDevicesResource<Disk, Disks> {

    private String getCommand;

    public PowerShellReadOnlyDisksResource(String parentId,
                                           PowerShellPoolMap shellPools,
                                           PowerShellParser parser,
                                           String getCommand,
                                           UriInfoProvider uriProvider) {
        super(parentId, shellPools, parser, uriProvider);
        this.getCommand = getCommand;
    }

    public List<Disk> runAndParse(String command) {
        List<Disk> ret = new ArrayList<Disk>();
        for (Disk disk : PowerShellDisk.parse(getParser(), parentId, PowerShellCmd.runCommand(getPool(), command))) {
            ret.add(disk);
        }
        return ret;
    }

    public Disk runAndParseSingle(String command) {
        List<Disk> disks = runAndParse(command);

        return !disks.isEmpty() ? disks.get(0) : null;
    }

    @Override
    public List<Disk> getDevices() {
        StringBuilder buf = new StringBuilder();

        buf.append(PowerShellVmsResource.getStorageDomainLookupHack(parentId));

        buf.append("$v = " + getCommand + " " + PowerShellUtils.escape(parentId) + ";");
        buf.append("$v.GetDiskImages()");

        return runAndParse(buf.toString());
    }

    @Override
    public Disk addLinks(Disk disk) {
        disk = JAXBHelper.clone("disk", Disk.class, disk);
        return LinkHelper.addLinks(getUriInfo(), disk);
    }

    @Override
    public Disks list() {
        Disks disks = new Disks();
        for (Disk disk : getDevices()) {
            disks.getDisks().add(addLinks(disk));
        }
        return disks;
    }

    @Override
    public PowerShellReadOnlyDeviceResource<Disk, Disks> getDeviceSubResource(String id) {
        return new PowerShellReadOnlyDeviceResource<Disk, Disks>(this, id);
    }
}

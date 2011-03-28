/*
 * Copyright © 2011 Red Hat, Inc.
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
package com.redhat.rhevm.api.powershell.enums;

import java.util.HashMap;

import com.redhat.rhevm.api.model.DiskInterface;

public enum PowerShellDiskInterface {
    IDE(DiskInterface.IDE),
    SCSI(DiskInterface.SCSI),
    VirtIO(DiskInterface.VIRTIO);

    private static HashMap<DiskInterface, PowerShellDiskInterface> mapping;
    static {
        mapping = new HashMap<DiskInterface, PowerShellDiskInterface>();
        for (PowerShellDiskInterface value : values()) {
            mapping.put(value.model, value);
        }
    }

    private DiskInterface model;

    private PowerShellDiskInterface(DiskInterface model) {
        this.model = model;
    }

    public DiskInterface map() {
        return model;
    }

    public static PowerShellDiskInterface forModel(DiskInterface model) {
        return mapping.get(model);
    }
}

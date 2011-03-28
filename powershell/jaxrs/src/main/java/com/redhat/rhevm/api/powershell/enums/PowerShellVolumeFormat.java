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
package com.redhat.rhevm.api.powershell.enums;

import java.util.HashMap;

import com.redhat.rhevm.api.model.DiskFormat;

public enum PowerShellVolumeFormat {
    Unassigned(3, null),
    COW(4, DiskFormat.COW),
    RAW(5, DiskFormat.RAW);

    private static HashMap<Integer, PowerShellVolumeFormat> mapping;
    static {
        mapping = new HashMap<Integer, PowerShellVolumeFormat>();
        for (PowerShellVolumeFormat value : values()) {
            mapping.put(value.value, value);
        }
    }

    private int value;
    private DiskFormat model;

    private PowerShellVolumeFormat(int value, DiskFormat model) {
        this.value = value;
        this.model = model;
    }

    public int getValue() {
        return value;
    }

    public DiskFormat map() {
        return model;
    }

    public static PowerShellVolumeFormat forValue(int value) {
        return mapping.get(value);
    }
}

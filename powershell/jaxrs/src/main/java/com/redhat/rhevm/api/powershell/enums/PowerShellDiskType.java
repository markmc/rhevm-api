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

import com.redhat.rhevm.api.model.DiskType;

public enum PowerShellDiskType {
    Unassigned(0), System(1), Data(2), Shared(3), Swap(4), Temp(5);

    private static HashMap<Integer, PowerShellDiskType> mapping;
    static {
        mapping = new HashMap<Integer, PowerShellDiskType>();
        for (PowerShellDiskType value : values()) {
            mapping.put(value.value, value);
        }
    }

    private int value;

    private PowerShellDiskType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public DiskType map() {
        switch (this) {
        case System:
            return DiskType.SYSTEM;
        case Data:
            return DiskType.DATA;
        case Shared:
            return DiskType.SHARED;
        case Swap:
            return DiskType.SWAP;
        case Temp:
            return DiskType.TEMP;
        default:
            assert false : this;
            return null;
        }
    }

    public static PowerShellDiskType forValue(int value) {
        return mapping.get(value);
    }
}

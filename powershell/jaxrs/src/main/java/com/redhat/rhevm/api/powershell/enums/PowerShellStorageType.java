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

import com.redhat.rhevm.api.model.StorageType;

public enum PowerShellStorageType {
    UNKNOWN(0, null),
    NFS(1, StorageType.NFS),
    FCP(2, StorageType.FCP),
    ISCSI(3, StorageType.ISCSI),
    ALL(4, null);

    private static HashMap<Integer, PowerShellStorageType> mapping;
    static {
        mapping = new HashMap<Integer, PowerShellStorageType>();
        for (PowerShellStorageType value : values()) {
            mapping.put(value.value, value);
        }
    }

    private int value;
    private StorageType model;

    private PowerShellStorageType(int value, StorageType model) {
        this.value = value;
        this.model = model;
    }

    public int getValue() {
        return value;
    }

    public StorageType map() {
        return model;
    }

    public static PowerShellStorageType forValue(int value) {
        return mapping.get(value);
    }
}

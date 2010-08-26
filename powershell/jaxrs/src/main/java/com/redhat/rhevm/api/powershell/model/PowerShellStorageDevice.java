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

import com.redhat.rhevm.api.model.StorageType;
import com.redhat.rhevm.api.powershell.enums.PowerShellStorageType;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;

public class PowerShellStorageDevice {

    private String id;
    private StorageType type;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public StorageType getType() {
        return type;
    }
    public void setType(StorageType type) {
        this.type = type;
    }

    public static List<PowerShellStorageDevice> parse(PowerShellParser parser, String output) {
        List<PowerShellStorageDevice> ret = new ArrayList<PowerShellStorageDevice>();

        for (PowerShellParser.Entity entity : parser.parse(output)) {
            PowerShellStorageDevice device = new PowerShellStorageDevice();

            device.setId(entity.get("id"));
            device.setType(entity.get("luntype", PowerShellStorageType.class).map());

            ret.add(device);
        }

        return ret;
    }
}

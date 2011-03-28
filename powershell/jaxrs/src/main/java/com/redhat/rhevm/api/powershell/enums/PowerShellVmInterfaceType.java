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

import com.redhat.rhevm.api.model.NicType;

public enum PowerShellVmInterfaceType {
    rtl8139_pv(NicType.RTL8139_VIRTIO),
    rtl8139(NicType.RTL8139),
    e1000(NicType.E1000),
    pv(NicType.VIRTIO);

    private static HashMap<NicType, PowerShellVmInterfaceType> mapping;
    static {
        mapping = new HashMap<NicType, PowerShellVmInterfaceType>();
        for (PowerShellVmInterfaceType value : values()) {
            mapping.put(value.model, value);
        }
    }

    private NicType model;

    private PowerShellVmInterfaceType(NicType model) {
        this.model = model;
    }

    public NicType map() {
        return model;
    }

    public static PowerShellVmInterfaceType forModel(NicType model) {
        return mapping.get(model);
    }
}

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.redhat.rhevm.api.model.BootDevice;
import com.redhat.rhevm.api.model.OperatingSystem;

public enum PowerShellBootSequence {
    C(0),    DC(1),  N(2),
    CDN(3),  CND(4), DCN(5),
    DNC(6),  NCD(7), NDC(8),
    CD(9),   D(10),  CN(11),
    DN(12),  NC(13), ND(14);

    private static HashMap<Integer, PowerShellBootSequence> mapping;
    static {
        mapping = new HashMap<Integer, PowerShellBootSequence>();
        for (PowerShellBootSequence value : values()) {
            mapping.put(value.value, value);
        }
    }

    private int value;

    private PowerShellBootSequence(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public List<OperatingSystem.Boot> map() {
        List<OperatingSystem.Boot> boots = new ArrayList<OperatingSystem.Boot>();
        switch (this) {
        case C:
            boots.add(getBoot(BootDevice.HD));
            break;
        case DC:
            boots.add(getBoot(BootDevice.CDROM));
            boots.add(getBoot(BootDevice.HD));
            break;
        case N:
            boots.add(getBoot(BootDevice.NETWORK));
            break;
        case CDN:
            boots.add(getBoot(BootDevice.HD));
            boots.add(getBoot(BootDevice.CDROM));
            boots.add(getBoot(BootDevice.NETWORK));
            break;
        case CND:
            boots.add(getBoot(BootDevice.HD));
            boots.add(getBoot(BootDevice.NETWORK));
            boots.add(getBoot(BootDevice.CDROM));
            break;
        case DCN:
            boots.add(getBoot(BootDevice.CDROM));
            boots.add(getBoot(BootDevice.HD));
            boots.add(getBoot(BootDevice.NETWORK));
            break;
        case DNC:
            boots.add(getBoot(BootDevice.CDROM));
            boots.add(getBoot(BootDevice.NETWORK));
            boots.add(getBoot(BootDevice.HD));
            break;
        case NCD:
            boots.add(getBoot(BootDevice.NETWORK));
            boots.add(getBoot(BootDevice.HD));
            boots.add(getBoot(BootDevice.CDROM));
            break;
        case NDC:
            boots.add(getBoot(BootDevice.NETWORK));
            boots.add(getBoot(BootDevice.CDROM));
            boots.add(getBoot(BootDevice.HD));
            break;
        case CD:
            boots.add(getBoot(BootDevice.HD));
            boots.add(getBoot(BootDevice.CDROM));
            break;
        case D:
            boots.add(getBoot(BootDevice.CDROM));
            break;
        case CN:
            boots.add(getBoot(BootDevice.HD));
            boots.add(getBoot(BootDevice.NETWORK));
            break;
        case DN:
            boots.add(getBoot(BootDevice.CDROM));
            boots.add(getBoot(BootDevice.NETWORK));
            break;
        case NC:
            boots.add(getBoot(BootDevice.NETWORK));
            boots.add(getBoot(BootDevice.HD));
            break;
        case ND:
            boots.add(getBoot(BootDevice.NETWORK));
            boots.add(getBoot(BootDevice.CDROM));
            break;
        default:
            assert false : this;
            break;
        }
        return boots;
    }

    private OperatingSystem.Boot getBoot(BootDevice device) {
        OperatingSystem.Boot boot = new OperatingSystem.Boot();
        boot.setDev(device);
        return boot;
    }

    public static PowerShellBootSequence forValue(int value) {
        return mapping.get(value);
    }
}

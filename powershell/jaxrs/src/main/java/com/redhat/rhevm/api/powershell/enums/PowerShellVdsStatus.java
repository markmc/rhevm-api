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

import com.redhat.rhevm.api.model.HostState;
import com.redhat.rhevm.api.model.HostStatus;

public enum PowerShellVdsStatus {
    Down("Down", HostState.DOWN),
    Error("Error", HostState.ERROR),
    Initializing("Initializing", HostState.ERROR),
    Installing("Installing", HostState.INACTIVE),
    Install_Failed("Install Failed", HostState.ERROR),
    Maintenance("Maintenance", HostState.INACTIVE),
    Non_Operational("Non Operational", HostState.ERROR),
    Non_Responsive("Non Responsive", HostState.ERROR),
    Pending_Approval("Pending Approval", HostState.PENDING_APPROVAL),
    Preparing_For_Maintenance("Preparing For Maintenance", HostState.INACTIVE),
    Problematic("Non-Responsive", HostState.ERROR),
    Reboot("Reboot", HostState.DOWN),
    Unassigned("Unassigned", HostState.INACTIVE),
    Up("Up", HostState.ACTIVE);

    private static HashMap<String, PowerShellVdsStatus> mapping;
    static {
        mapping = new HashMap<String, PowerShellVdsStatus>();
        for (PowerShellVdsStatus value : values()) {
            mapping.put(value.value, value);
        }
    }

    private String value;
    private HostState state;

    private PowerShellVdsStatus(String value, HostState state) {
        this.value = value;
        this.state = state;
    }

    public static PowerShellVdsStatus forValue(String value) {
        return mapping.get(value);
    }

    public HostStatus map() {
        HostStatus ret = new HostStatus();
        ret.setState(state);
        ret.setDetail(value.toLowerCase());
        return ret;
    }
}

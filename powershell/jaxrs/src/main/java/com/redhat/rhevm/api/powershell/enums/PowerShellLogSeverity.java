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

import com.redhat.rhevm.api.model.LogSeverity;

public enum PowerShellLogSeverity {
    NORMAL(0), WARNING(1), ERROR(2), ALERT(10);

    private static HashMap<Integer, PowerShellLogSeverity> mapping;
    static {
        mapping = new HashMap<Integer, PowerShellLogSeverity>();
        for (PowerShellLogSeverity value : values()) {
            mapping.put(value.value, value);
        }
    }

    private int value;

    private PowerShellLogSeverity(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public LogSeverity map() {
        switch (this) {
        case NORMAL:
            return LogSeverity.NORMAL;
        case WARNING:
            return LogSeverity.WARNING;
        case ERROR:
            return LogSeverity.ERROR;
        case ALERT:
            return LogSeverity.ALERT;
        default:
            assert false : this;
            return null;
        }
    }

    public static PowerShellLogSeverity forValue(int value) {
        return mapping.get(value);
    }
}

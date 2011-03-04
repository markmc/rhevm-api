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

public enum PowerShellAsyncTaskResult {

    success(0),
    failure(1),
    cleanSuccess(2),
    cleanFailure(3);

    private static HashMap<Integer, PowerShellAsyncTaskResult> mapping;
    static {
        mapping = new HashMap<Integer, PowerShellAsyncTaskResult>();
        for (PowerShellAsyncTaskResult value : values()) {
            mapping.put(value.getValue(), value);
        }
    }

    private int value;

    private PowerShellAsyncTaskResult(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static PowerShellAsyncTaskResult forValue(int value) {
        return mapping.get(value);
    }
}

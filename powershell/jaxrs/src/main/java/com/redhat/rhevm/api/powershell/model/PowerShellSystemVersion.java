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

import com.redhat.rhevm.api.model.SystemVersion;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;

public class PowerShellSystemVersion {

    private static SystemVersion parseVersion(String text) {
        SystemVersion version = new SystemVersion();
        String parts[] = text.split("\\.", -1);
        switch (parts.length) {
        case 4:
            version.setRevision(Integer.parseInt(parts[3]));
        case 3:
            version.setBuild(Integer.parseInt(parts[2]));
        case 2:
            version.setMinor(Integer.parseInt(parts[1]));
        case 1:
            version.setMajor(Integer.parseInt(parts[0]));
        }
        return version;
    }

    public static List<SystemVersion> parse(PowerShellParser parser, String output) {
        List<SystemVersion> ret = new ArrayList<SystemVersion>();

        for (PowerShellParser.Entity entity : parser.parse(output)) {
            if (entity.getType().equals("System.Version")) {
                ret.add(parseVersion(entity.getValue()));
            }
        }

        return ret;
    }
}

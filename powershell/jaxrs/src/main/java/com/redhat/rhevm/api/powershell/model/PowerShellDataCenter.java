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

import com.redhat.rhevm.api.model.DataCenter;
import com.redhat.rhevm.api.model.DataCenterStatus;
import com.redhat.rhevm.api.model.Version;
import com.redhat.rhevm.api.powershell.enums.PowerShellStorageType;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;

public class PowerShellDataCenter {

    public static List<DataCenter> parse(PowerShellParser parser, String output) {
        List<DataCenter> ret = new ArrayList<DataCenter>();

        for (PowerShellParser.Entity entity : parser.parse(output)) {
            DataCenter dataCenter = new DataCenter();

            dataCenter.setId(entity.get("datacenterid"));
            dataCenter.setName(entity.get("name"));
            dataCenter.setDescription(entity.get("description"));
            dataCenter.setStorageType(entity.get("type", PowerShellStorageType.class).map());
            dataCenter.setStatus(parseStatus(entity.get("status")));
            dataCenter.setVersion(entity.get("compatibilityversion", Version.class));

            ret.add(dataCenter);
        }

        return ret;
    }

    private static DataCenterStatus parseStatus(String s) {
        if (s.equals("Uninitialized"))   return DataCenterStatus.UNINITIALIZED;
        if (s.equals("Up"))              return DataCenterStatus.UP;
        if (s.equals("Maintenance"))     return DataCenterStatus.MAINTENANCE;
        if (s.equals("Not Operational")) return DataCenterStatus.NOT_OPERATIONAL;
        if (s.equals("Non-Responsive"))  return DataCenterStatus.PROBLEMATIC;
        if (s.equals("Contend"))         return DataCenterStatus.CONTEND;
        else assert false : s;
        return null;
    }
}

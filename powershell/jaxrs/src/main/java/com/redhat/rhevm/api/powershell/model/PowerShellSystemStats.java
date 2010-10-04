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

import java.util.List;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;

public class PowerShellSystemStats {

    public long totalVms, activeVms;
    public long totalHosts, activeHosts;
    public long totalUsers, activeUsers;
    public long totalStorageDomains, activeStorageDomains;

    public static PowerShellSystemStats parse(PowerShellParser parser, String output) {
        PowerShellSystemStats stats = new PowerShellSystemStats();

        List<PowerShellParser.Entity> entities = parser.parse(output);
        if (entities.size() > 0) {
            PowerShellParser.Entity entity = entities.get(0);

            stats.totalVms = entity.get("totalvms", Integer.class);
            stats.activeVms = entity.get("activevms", Integer.class);
            stats.totalHosts = entity.get("totalhosts", Integer.class);
            stats.activeHosts = entity.get("activehosts", Integer.class);
            stats.totalUsers = entity.get("totalusers", Integer.class);
            stats.activeUsers = entity.get("activeusers", Integer.class);
            stats.totalStorageDomains = entity.get("totalstoragedomains", Integer.class);
            stats.activeStorageDomains = entity.get("activestoragedomains", Integer.class);
        }

        return stats;
    }
}

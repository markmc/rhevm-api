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
package com.redhat.rhevm.api.powershell.model;

import java.util.ArrayList;
import java.util.List;

import com.redhat.rhevm.api.model.Group;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;

public class PowerShellGroup {

    private static String TYPE = "RhevmCmd.CLIUser";

    public static List<Group> parse(PowerShellParser parser, String output) {
        List<Group> ret = new ArrayList<Group>();

        for (PowerShellParser.Entity entity : parser.parse(output)) {
            if (TYPE.equals(entity.getType())) {
                Group group = new Group();
                group.setId(entity.get("userid"));
                group.setName(entity.get("name"));
                ret.add(group);
            }
        }

        return ret;
    }
}

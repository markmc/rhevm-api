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

import com.redhat.rhevm.api.model.Ticket;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;

public class PowerShellTicket {

    public static Ticket parse(PowerShellParser parser, String output) {
        List<PowerShellParser.Entity> entities = parser.parse(output);
        Ticket ticket = null;
        if (!(entities == null || entities.isEmpty())) {
            PowerShellParser.Entity entity = entities.get(0);
            ticket = new Ticket();
            ticket.setValue(entity.get("ticket"));
            ticket.setExpiry((long)entity.get("validtime", Integer.class));
        }
        return ticket;
    }
}

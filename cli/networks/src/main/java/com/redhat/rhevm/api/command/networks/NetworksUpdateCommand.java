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
package com.redhat.rhevm.api.command.networks;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;

import com.redhat.rhevm.api.command.base.AbstractUpdateCommand;
import com.redhat.rhevm.api.model.Network;
import com.redhat.rhevm.api.model.Networks;

/**
 * Update a Network
 */
@Command(scope = "networks", name = "update", description = "Update Virtual Machine state")
public class NetworksUpdateCommand extends AbstractUpdateCommand<Network> {

    @Argument(index = 0, name = "name", description = "The name of the Network", required = true, multiValued = false)
    protected String name;

    protected Object doExecute() throws Exception {
        doUpdate(client.getCollection("networks", Networks.class).getNetworks(), Network.class, "network", name);
        return null;
    }
}


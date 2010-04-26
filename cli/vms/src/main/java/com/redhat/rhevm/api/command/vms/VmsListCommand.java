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
package com.redhat.rhevm.api.command.vms;

import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;

import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.model.VMs;

/**
 * Displays the VMs
 */
@Command(scope = "vms", name = "list", description = "Lists Virtual Machines.")
public class VmsListCommand extends AbstractVmsCommand {

    @Option(name = "-b", aliases = {"--bound"}, description="Upper bound on number of VMs to display", required = false, multiValued = false)
    protected int limit = Integer.MAX_VALUE;

    protected Object doExecute() throws Exception {
        VMs vms = client.getCollection("vms", VMs.class);
        int i = 0;
        for (VM vm : vms.getVMs()) {
            if (++i > limit) {
                break;
            }
            System.out.println("[ " + vm.getName() + "] [" + (vm.getId().length() < "ID".length() ? " " : "") + vm.getId() + "]" );
        }
        return null;
    }
}

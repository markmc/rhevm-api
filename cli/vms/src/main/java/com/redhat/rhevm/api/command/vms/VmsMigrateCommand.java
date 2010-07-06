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

import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.Host;

/**
 * Migrate a VM
 */
@Command(scope = "vms", name = "migrate", description = "Migrate a Virtual Machine.")
public class VmsMigrateCommand extends AbstractVmsActionCommand {

    @Option(name = "-h", aliases = { "--host" }, description = "Host name", required = true, multiValued = false)
    private String host;

    protected Object doExecute() throws Exception {
        Action action = new Action();
        action.setHost(new Host());
        action.getHost().setName(host);
        doAction("migrate", action);
        return null;
    }
}

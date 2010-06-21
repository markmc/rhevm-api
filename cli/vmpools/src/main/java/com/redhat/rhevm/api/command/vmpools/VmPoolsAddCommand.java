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
package com.redhat.rhevm.api.command.vmpools;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;

import com.redhat.rhevm.api.command.base.AbstractAddCommand;
import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.model.VmPool;

/**
 * Add a new VM Pool.
 */
@Command(scope = "vmpools", name = "add", description = "Add a new VM Pool")
public class VmPoolsAddCommand extends AbstractAddCommand<VmPool> {

    @Argument(index = 0, name = "name", description = "Name of the VM Pool to add", required = true, multiValued = false)
    protected String name;

    @Option(name = "-t", aliases = { "--template" }, description = "Template name", required = true, multiValued = false)
    private String template;

    @Option(name = "-c", aliases = { "--cluster" }, description = "Cluster name", required = true, multiValued = false)
    private String cluster;

    @Option(name = "-s", aliases = { "--size" }, description = "Number of VMs", required = false, multiValued = false)
    private int size = -1;

    protected Object doExecute() throws Exception {
        display(doAdd(getModel(), VmPool.class, "vmpools", "vmpool"));
        return null;
    }

    protected VmPool getModel() {
        VmPool model = new VmPool();
        model.setName(name);
        model.setCluster(new Cluster());
        model.getCluster().setName(cluster);
        model.setTemplate(new Template());
        model.getTemplate().setName(template);
        if (size != -1) {
            model.setSize(size);
        }
        return model;
    }
}

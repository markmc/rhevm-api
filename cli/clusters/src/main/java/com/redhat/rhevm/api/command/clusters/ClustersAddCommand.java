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
package com.redhat.rhevm.api.command.clusters;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;

import com.redhat.rhevm.api.command.base.AbstractAddCommand;
import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.CPU;
import com.redhat.rhevm.api.model.DataCenter;

/**
 * Add a new Cluster.
 */
@Command(scope = "clusters", name = "add", description = "Add a new Cluster")
public class ClustersAddCommand extends AbstractAddCommand<Cluster> {

    @Argument(index = 0, name = "name", description = "Name of the Cluster to add", required = true, multiValued = false)
    protected String name;

    @Option(name = "-c", aliases = { "--cpu" }, description = "Cluster CPU name", required = true, multiValued = false)
    private String cpu;

    @Option(name = "-d", aliases = { "--datacenter" }, description = "Data center ID", required = true, multiValued = false)
    private String datacenter;

    protected Object doExecute() throws Exception {
        display(doAdd(getModel(), Cluster.class, "clusters", "cluster"));
        return null;
    }

    protected Cluster getModel() {
        Cluster model = new Cluster();
        model.setName(name);
        model.setCpu(new CPU());
        model.getCpu().setId(cpu);
        model.setDataCenter(new DataCenter());
        model.getDataCenter().setId(datacenter);
        return model;
    }
}

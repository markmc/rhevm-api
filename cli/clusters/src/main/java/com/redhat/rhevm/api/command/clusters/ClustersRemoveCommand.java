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

import com.redhat.rhevm.api.command.base.AbstractRemoveCommand;
import com.redhat.rhevm.api.model.BaseResource;
import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.Clusters;

/**
 * Removes a Cluster
 */
@Command(scope = "clusters", name = "remove", description = "Removes a Cluster.")
public class ClustersRemoveCommand extends AbstractRemoveCommand {

    @Argument(index = 0, name = "name", description = "The name of the Cluster", required = true, multiValued = false)
    protected String name;

    protected Object doExecute() throws Exception {
        doRemove(client.getCollection("clusters", Clusters.class).getClusters(), Cluster.class, name);
        return null;
    }
}

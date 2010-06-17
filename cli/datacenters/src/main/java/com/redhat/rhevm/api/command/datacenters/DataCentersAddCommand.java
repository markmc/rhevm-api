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
package com.redhat.rhevm.api.command.datacenters;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;

import com.redhat.rhevm.api.command.base.AbstractAddCommand;

import com.redhat.rhevm.api.model.DataCenter;
import com.redhat.rhevm.api.model.StorageType;

/**
 * Add a new Data Center.
 */
@Command(scope = "datacenters", name = "add", description = "Add a new Data Center")
public class DataCentersAddCommand extends AbstractAddCommand<DataCenter> {

    @Argument(index = 0, name = "name", description = "Name of the Data Center to add", required = true, multiValued = false)
    protected String name;

    @Option(name = "-t", aliases = { "--type" }, description = "Storage type (ISCSI, FCP, or NFS)", required = true, multiValued = false)
    private String type;

    protected Object doExecute() throws Exception {
        display(doAdd(getModel(), DataCenter.class, "datacenters", "data_center"));
        return null;
    }

    protected DataCenter getModel() {
        DataCenter model = new DataCenter();
        model.setName(name);
        model.setStorageType(StorageType.valueOf(type));
        return model;
    }
}

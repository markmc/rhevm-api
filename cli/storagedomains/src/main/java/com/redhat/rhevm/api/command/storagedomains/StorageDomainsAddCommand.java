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
package com.redhat.rhevm.api.command.storagedomains;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;

import com.redhat.rhevm.api.command.base.AbstractAddCommand;
import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.model.LogicalUnit;
import com.redhat.rhevm.api.model.Storage;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.StorageDomainType;
import com.redhat.rhevm.api.model.StorageType;

/**
 * Add a new StorageDomain.
 */
@Command(scope = "storagedomains", name = "add", description = "Add a new Storage Domain")
public class StorageDomainsAddCommand extends AbstractAddCommand<StorageDomain> {

    @Argument(index = 0, name = "name", description = "Name of the Storage Domain to add", required = true, multiValued = false)
    protected String name;

    @Option(name = "-h", aliases = { "--host" }, description = "Name of initialization host", required = true, multiValued = false)
    private String host;

    @Option(name = "-i", aliases = { "--id" }, description = "Storage Domain ID", required = true, multiValued = false)
    private String id;

    @Option(name = "-m", aliases = { "--master" }, description = "Is master domain (true/false)", required = false, multiValued = false)
    private boolean master;

    @Option(name = "-d", aliases = { "--domain-type" }, description = "Storage Domain type (DATA, ISO or EXPORT)", required = true, multiValued = false)
    private String domainType;

    @Option(name = "-s", aliases = { "--storage-type" }, description = "Storage type (ISCSI, FCP or NFS)", required = true, multiValued = false)
    private String storageType;

    @Option(name = "-a", aliases = { "--address" }, description = "Storage address", required = false, multiValued = false)
    private String address;

    @Option(name = "-p", aliases = { "--path" }, description = "Storage path", required = false, multiValued = false)
    private String path;

    @Option(name = "-t", aliases = { "--target" }, description = "iSCSI target", required = false, multiValued = false)
    private String target;

    @Option(name = "-l", aliases = { "--lunid" }, description = "LUN ID", required = false, multiValued = false)
    private String lunId;

    protected Object doExecute() throws Exception {
        display(doAdd(getModel(), StorageDomain.class, "storagedomains", "storage_domain"));
        return null;
    }

    protected StorageDomain getModel() {
        StorageDomain model = new StorageDomain();
        model.setId(id);
        model.setName(name);
        model.setHost(new Host());
        model.getHost().setName(host);
        model.setType(StorageDomainType.valueOf(domainType));
        model.setStorage(new Storage());
        model.getStorage().setType(StorageType.valueOf(storageType));
        switch (model.getStorage().getType()) {
        case NFS:
            model.getStorage().setAddress(address);
            model.getStorage().setPath(path);
            break;
        case ISCSI:
            model.getStorage().getLogicalUnits().add(new LogicalUnit());
            model.getStorage().getLogicalUnits().get(0).setId(lunId);
            model.getStorage().getLogicalUnits().get(0).setAddress(address);
            model.getStorage().getLogicalUnits().get(0).setTarget(target);
            break;
        case FCP:
        default:
            break;
        }
        return model;
    }
}

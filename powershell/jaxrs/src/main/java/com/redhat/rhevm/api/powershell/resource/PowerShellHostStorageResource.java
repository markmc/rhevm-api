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
package com.redhat.rhevm.api.powershell.resource;

import java.util.List;
import java.util.concurrent.Executor;

import javax.ws.rs.Produces;

import com.redhat.rhevm.api.resource.MediaType;

import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.model.HostStorage;
import com.redhat.rhevm.api.model.LogicalUnit;
import com.redhat.rhevm.api.model.Storage;
import com.redhat.rhevm.api.model.VolumeGroup;
import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.powershell.model.PowerShellStorageDevice;
import com.redhat.rhevm.api.powershell.model.PowerShellStorageDomain;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
import com.redhat.rhevm.api.resource.StorageResource;
import com.redhat.rhevm.api.resource.HostStorageResource;

@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class PowerShellHostStorageResource
    extends UriProviderWrapper
    implements HostStorageResource {

    protected String hostId;

    public PowerShellHostStorageResource(String hostId,
                                         Executor executor,
                                         PowerShellPoolMap shellPools,
                                         PowerShellParser parser,
                                         UriInfoProvider uriProvider) {
        super(executor, shellPools, parser, uriProvider);
        this.hostId = hostId;
    }

    public String getHostId() {
        return hostId;
    }

    public List<PowerShellStorageDevice> runAndParseDevices(String command) {
        return PowerShellStorageDevice.parse(getParser(), PowerShellCmd.runCommand(getPool(), command));
    }

    public PowerShellStorageDevice runAndParseDevice(String command) {
        List<PowerShellStorageDevice> devices = runAndParseDevices(command);
        return !devices.isEmpty() ? devices.get(0) : null;
    }

    public List<PowerShellStorageDomain> runAndParseStorageDomains(String command) {
        return PowerShellStorageDomain.parse(getParser(), PowerShellCmd.runCommand(getPool(), command));
    }

    public PowerShellStorageDomain runAndParseStorageDomain(String command) {
        List<PowerShellStorageDomain> storageDomains = runAndParseStorageDomains(command);
        return !storageDomains.isEmpty() ? storageDomains.get(0) : null;
    }

    private Storage buildStorageFromDevice(PowerShellStorageDevice device) {
        Storage storage = new Storage();
        storage.setId(device.getId());
        storage.setType(device.getType());

        LogicalUnit lu = new LogicalUnit();
        lu.setId(device.getId());

        storage.getLogicalUnits().add(lu);

        return storage;
    }

    private Storage buildStorageFromStorageDomain(PowerShellStorageDomain storageDomain) {
        Storage storage = new Storage();
        storage.setId(storageDomain.getVgId());
        storage.setType(storageDomain.getStorage().getType());

        storage.setVolumeGroup(new VolumeGroup());
        storage.getVolumeGroup().setId(storageDomain.getVgId());

        return storage;
    }

    public Storage getStorage(String id) {
        StringBuilder buf = new StringBuilder();

        buf.append("get-storagedevices -hostid " + PowerShellUtils.escape(hostId));
        buf.append(" | ? { $_.id -eq " + PowerShellUtils.escape(id) + " }");

        PowerShellStorageDevice device = runAndParseDevice(buf.toString());
        if (device != null) {
            return buildStorageFromDevice(device);
        }

        buf = new StringBuilder();

        buf.append("get-storagevolumegroups -hostid " + PowerShellUtils.escape(hostId));
        buf.append(" | ? { $_.vgid -eq " + PowerShellUtils.escape(id) + " }");

        PowerShellStorageDomain storageDomain = runAndParseStorageDomain(buf.toString());
        if (storageDomain != null) {
            return buildStorageFromStorageDomain(storageDomain);
        }

        return null;
    }

    public Storage addLinks(Storage storage) {
        storage.setHost(new Host());
        storage.getHost().setId(hostId);

        return LinkHelper.addLinks(getUriInfo(), storage);
    }

    @Override
    public HostStorage list() {
        HostStorage ret = new HostStorage();

        StringBuilder buf = new StringBuilder();

        buf.append("get-storagedevices -hostid " + PowerShellUtils.escape(hostId));

        for (PowerShellStorageDevice device : runAndParseDevices(buf.toString())) {
            ret.getStorage().add(addLinks(buildStorageFromDevice(device)));
        }

        buf = new StringBuilder();

        buf.append("get-storagevolumegroups -hostid " + PowerShellUtils.escape(hostId));

        for (PowerShellStorageDomain storageDomain : runAndParseStorageDomains(buf.toString())) {
            ret.getStorage().add(addLinks(buildStorageFromStorageDomain(storageDomain)));
        }

        return ret;
    }

    @Override
    public StorageResource getStorageSubResource(String id) {
        return new PowerShellStorageResource(id, this);
    }
}

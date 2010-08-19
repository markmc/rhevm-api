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

import java.util.ArrayList;
import java.util.List;

import com.redhat.rhevm.api.model.Storage;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.StorageDomainStatus;
import com.redhat.rhevm.api.model.StorageDomainType;
import com.redhat.rhevm.api.model.StorageType;
import com.redhat.rhevm.api.powershell.enums.PowerShellStorageDomainSharedStatus;
import com.redhat.rhevm.api.powershell.enums.PowerShellStorageDomainStatus;
import com.redhat.rhevm.api.powershell.enums.PowerShellStorageType;
import com.redhat.rhevm.api.powershell.model.PowerShellStorageDomain;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;

public class PowerShellStorageDomain extends StorageDomain {

    private StorageDomainStatus sharedStatus;

    public StorageDomainStatus getSharedStatus() {
        return sharedStatus;
    }
    public void setSharedStatus(StorageDomainStatus sharedStatus) {
        this.sharedStatus = sharedStatus;
    }

    public static List<PowerShellStorageDomain> parse(PowerShellParser parser, String output) {
        List<PowerShellStorageDomain> ret = new ArrayList<PowerShellStorageDomain>();

        for (PowerShellParser.Entity entity : parser.parse(output)) {
            PowerShellStorageDomain storageDomain = new PowerShellStorageDomain();

            storageDomain.setId(entity.get("storagedomainid"));
            storageDomain.setName(entity.get("name"));

            String domainType = entity.get("domaintype").toUpperCase();
            if (domainType.endsWith(" (MASTER)")) {
                domainType = domainType.split(" ")[0];
                storageDomain.setMaster(true);
            }
            storageDomain.setType(StorageDomainType.fromValue(domainType));

            PowerShellStorageDomainStatus status =
                entity.get("status", PowerShellStorageDomainStatus.class);
            if (status != null) {
                storageDomain.setStatus(status.map());
            }

            PowerShellStorageDomainSharedStatus sharedStatus =
                entity.get("sharedstatus", PowerShellStorageDomainSharedStatus.class);
            if (sharedStatus != null) {
                storageDomain.setSharedStatus(sharedStatus.map());
            }

            Storage storage = new Storage();

            storage.setType(entity.get("type", PowerShellStorageType.class).map());

            switch (storage.getType()) {
            case NFS:
                String[] parts = entity.get("nfspath").split(":");
                storage.setAddress(parts[0]);
                storage.setPath(parts[1]);
                break;
            case ISCSI:
                break;
            case FCP:
            default:
                assert false : storage.getType();
                break;
            }

            storageDomain.setStorage(storage);

            ret.add(storageDomain);
        }

        return ret;
    }
}

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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.redhat.rhevm.api.model.Storage;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.StorageDomainType;
import com.redhat.rhevm.api.model.StorageType;
import com.redhat.rhevm.api.powershell.enums.PowerShellStorageDomainStatus;
import com.redhat.rhevm.api.powershell.enums.PowerShellStorageType;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;

public class PowerShellStorageDomain extends StorageDomain {

    private static final String STORAGE_DOMAIN_TYPE = "RhevmCmd.CLIStorageDomain";

    private String vgId;

    static boolean isStorageDomain(PowerShellParser.Entity entity) {
        return STORAGE_DOMAIN_TYPE.equals(entity.getType());
    }

    public String getVgId() {
        return vgId;
    }
    public void setVgId(String vgId) {
        this.vgId = vgId;
    }

    public static List<PowerShellStorageDomain> parse(PowerShellParser parser, String output) {
        List<PowerShellStorageDomain> ret = new ArrayList<PowerShellStorageDomain>();

        for (PowerShellParser.Entity entity : parser.parse(output)) {
            if (isStorageDomain(entity)) {
                ret.add(parseEntity(entity));
            }
        }

        return ret;
    }

    public static List<PowerShellStorageDomain> parseEntity(PowerShellParser.Entity entity) {
        PowerShellStorageDomain storageDomain = new PowerShellStorageDomain();

        storageDomain.setId(entity.get("storagedomainid"));
        storageDomain.setName(entity.get("name"));
        storageDomain.setVgId(entity.get("vgid"));

        String domainType = entity.get("domaintype").toUpperCase();
        storageDomain.setMaster(domainType.endsWith(" (MASTER)"));
        if (storageDomain.isMaster()) {
            domainType = domainType.split(" ")[0];
        }
        storageDomain.setType(StorageDomainType.valueOf(domainType).value());

        PowerShellStorageDomainStatus status =
            entity.get("status", PowerShellStorageDomainStatus.class);
        if (status != null) {
            storageDomain.setStatus(status.map());
        }

        Storage storage = new Storage();

        StorageType storageType = entity.get("type", PowerShellStorageType.class).map();

        storage.setType(storageType.value());

        switch (storageType) {
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

        parseUnsignedInt(entity, storageDomain, "availabledisksize", "Available");
        parseUnsignedInt(entity, storageDomain, "useddisksize", "Used");
        parseUnsignedInt(entity, storageDomain, "committeddisksize", "Committed");

        return storageDomain;
    }

    private static void parseUnsignedInt(PowerShellParser.Entity entity, PowerShellStorageDomain storageDomain, String property, String field) {
        Integer value = entity.get(property, Integer.class);
        if (value != null) {
            try {
                Method m = storageDomain.getClass().getMethod("set" + field, Long.class);
                m.invoke(storageDomain, Long.valueOf(value) * 1024 * 1024 * 1024);
            } catch (Exception e) {
                // simple setter shouldn't fail
            }
        }
    }
}

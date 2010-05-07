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

import java.util.ArrayList;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.ActionValidator;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.Link;
import com.redhat.rhevm.api.resource.AttachmentsResource;
import com.redhat.rhevm.api.resource.StorageDomainResource;
import com.redhat.rhevm.api.powershell.model.PowerShellStorageDomain;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;

public class PowerShellStorageDomainResource implements StorageDomainResource, ActionValidator {

    private StorageDomain storageDomain;

    public PowerShellStorageDomainResource(StorageDomain storageDomain) {
        this.storageDomain = storageDomain;
    }

    public PowerShellStorageDomainResource(String id) {
        storageDomain = new StorageDomain();
        storageDomain.setId(id);
    }

    public static ArrayList<StorageDomain> runAndParse(String command) {
        return PowerShellStorageDomain.parse(PowerShellUtils.runCommand(command));
    }

    public static StorageDomain runAndParseSingle(String command) {
        ArrayList<StorageDomain> storageDomains = runAndParse(command);

        return !storageDomains.isEmpty() ? storageDomains.get(0) : null;
    }

    public StorageDomain addLinks(UriBuilder uriBuilder) {
        storageDomain.setHref(uriBuilder.build().toString());
        return storageDomain;
    }

    @Override
    public StorageDomain get(UriInfo uriInfo) {
        storageDomain = runAndParseSingle("get-storagedomain " + storageDomain.getId());
        return addLinks(uriInfo.getRequestUriBuilder());
    }

    @Override
    public StorageDomain update(UriInfo uriInfo, StorageDomain storageDomain) {
        StringBuilder buf = new StringBuilder();

        buf.append("$h = get-storagedomain " + storageDomain.getId() + "\n");

        if (storageDomain.getName() != null) {
            buf.append("$h.name = \"" + storageDomain.getName() + "\"");
        }

        buf.append("\n");
        buf.append("update-storagedomain -storagedomainobject $v");

        this.storageDomain = runAndParseSingle(buf.toString());

        return addLinks(uriInfo.getRequestUriBuilder());
    }

    @Override
    public boolean validateAction(String action) {
        switch (storageDomain.getStatus()) {
        case UNINITIALIZED:
            return action.equals("initialize");
        case UNATTACHED:
        case ACTIVE:
        case INACTIVE:
            return false;
        case LOCKED:
        case MIXED:
        default:
            assert false : storageDomain.getStatus();
            return false;
        }
    }

    @Override
    public void initialize() {
    }

    @Override
    public void teardown() {
    }

    @Override
    public AttachmentsResource getAttachmentsResource() {
        return null;
    }
}

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

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.common.util.JAXBHelper;
import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.ActionsBuilder;
import com.redhat.rhevm.api.model.ActionValidator;
import com.redhat.rhevm.api.model.Link;
import com.redhat.rhevm.api.model.Storage;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.resource.AttachmentsResource;
import com.redhat.rhevm.api.resource.StorageDomainResource;
import com.redhat.rhevm.api.common.resource.AbstractActionableResource;
import com.redhat.rhevm.api.powershell.model.PowerShellStorageDomain;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;

public class PowerShellStorageDomainResource extends AbstractActionableResource<StorageDomain> implements StorageDomainResource {

    private PowerShellStorageDomainsResource parent;
    private StorageDomain staged;

    public PowerShellStorageDomainResource(String id,
                                           PowerShellStorageDomainsResource parent,
                                           StorageDomain staged) {
        super(id);
        this.parent = parent;
        this.staged = staged;
    }

    public PowerShellStorageDomainResource(String id, PowerShellStorageDomainsResource parent) {
        this(id, parent, null);
    }

    public StorageDomain getStaged() {
        return staged;
    }

    public static ArrayList<StorageDomain> runAndParse(String command) {
        return PowerShellStorageDomain.parse(PowerShellUtils.runCommand(command));
    }

    public static StorageDomain runAndParseSingle(String command) {
        ArrayList<StorageDomain> storageDomains = runAndParse(command);

        return !storageDomains.isEmpty() ? storageDomains.get(0) : null;
    }

    public StorageDomain addLinks(StorageDomain storageDomain, UriBuilder uriBuilder) {
        storageDomain.setHref(uriBuilder.build().toString());

        ActionValidator actionValidator = new StorageDomainActionValidator(storageDomain);
        ActionsBuilder actionsBuilder = new ActionsBuilder(uriBuilder, StorageDomainResource.class, actionValidator);
        storageDomain.setActions(actionsBuilder.build());

        Link link = new Link();
        link.setRel("attachments");
        link.setHref(uriBuilder.clone().path("attachments").build().toString());
        storageDomain.getLinks().clear();
        storageDomain.getLinks().add(link);

        return storageDomain;
    }

    @Override
    public StorageDomain get(UriInfo uriInfo) {
        StorageDomain storageDomain;
        if (staged != null) {
            storageDomain = JAXBHelper.clone(OBJECT_FACTORY.createStorageDomain(staged));
        } else {
            storageDomain = parent.mapFromRhevmId(runAndParseSingle("get-storagedomain " + getId()));
        }
        return addLinks(storageDomain, uriInfo.getRequestUriBuilder());
    }

    @Override
    public StorageDomain update(HttpHeaders headers, final UriInfo uriInfo, StorageDomain storageDomain) {
        validateUpdate(storageDomain, headers);

        StringBuilder buf = new StringBuilder();
        if (staged != null) {
            // update writable fields only
            staged.setName(storageDomain.getName());

            storageDomain = JAXBHelper.clone(OBJECT_FACTORY.createStorageDomain(staged));
        } else {
            buf.append("$h = get-storagedomain " + getId() + "\n");

            if (storageDomain.getName() != null) {
                buf.append("$h.name = \"" + storageDomain.getName() + "\"");
            }

            buf.append("\n");
            buf.append("update-storagedomain -storagedomainobject $v");

            storageDomain = parent.mapFromRhevmId(runAndParseSingle(buf.toString()));
        }

        return addLinks(storageDomain, uriInfo.getRequestUriBuilder());
    }

    @Override
    public void initialize(UriInfo uriInfo, Action action) {
        StringBuilder buf = new StringBuilder();

        buf.append("add-storagedomain");

        if (staged.getName() != null) {
            buf.append(" -name " + staged.getName());
        }

        buf.append(" -hostid " + action.getHost().getId());

        buf.append(" -domaintype ");
        switch (staged.getType()) {
        case DATA:
            buf.append("Data");
            break;
        case ISO:
            buf.append("ISO");
            break;
        case EXPORT:
            buf.append("Export");
            break;
        default:
            assert false : staged.getType();
            break;
        }

        Storage storage = staged.getStorage();

        buf.append(" -storagetype " + storage.getType().toString());
        buf.append(" -storage ");

        switch (storage.getType()) {
        case NFS:
            buf.append(storage.getHost() + ":" + storage.getPath());
            break;
        case ISCSI:
        case FCP:
        default:
            assert false : storage.getType();
            break;
        }

        StorageDomain storageDomain = PowerShellStorageDomainResource.runAndParseSingle(buf.toString());

        parent.unstageDomain(getId(), storageDomain.getId());
    }

    @Override
    public void teardown(UriInfo uriInfo, Action action) {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(getId());
        parent.mapToRhevmId(storageDomain);

        storageDomain = runAndParseSingle("get-storagedomain " + storageDomain.getId());

        StringBuilder buf = new StringBuilder();

        buf.append("remove-storagedomain --force");

        buf.append(" --storagedomainid " + storageDomain.getId());

        buf.append(" -hostid " + action.getHost().getId());

        PowerShellUtils.runCommand(buf.toString());

        staged = parent.mapFromRhevmId(storageDomain);
        parent.stageDomain(getId(), this);
    }

    @Override
    public AttachmentsResource getAttachmentsResource() {
        return new PowerShellAttachmentsResource(getId());
    }

    private class StorageDomainActionValidator implements ActionValidator {
        private StorageDomain storageDomain;

        public StorageDomainActionValidator(StorageDomain storageDomain) {
            this.storageDomain = storageDomain;
        }

        @Override
        public boolean validateAction(String action) {
            switch (storageDomain.getStatus()) {
            case UNINITIALIZED:
                return action.equals("initialize");
            case UNATTACHED:
                return action.equals("teardown");
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
    }
}

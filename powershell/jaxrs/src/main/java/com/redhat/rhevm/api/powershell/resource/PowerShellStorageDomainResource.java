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
import java.util.List;

import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.common.util.JAXBHelper;
import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.model.ActionsBuilder;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.model.Templates;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.model.VMs;
import com.redhat.rhevm.api.resource.AssignedPermissionsResource;
import com.redhat.rhevm.api.resource.StorageDomainContentsResource;
import com.redhat.rhevm.api.resource.StorageDomainResource;
import com.redhat.rhevm.api.powershell.model.PowerShellStorageDomain;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPool;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;

public class PowerShellStorageDomainResource extends AbstractPowerShellActionableResource<StorageDomain> implements StorageDomainResource {

    private PowerShellStorageDomainsResource parent;

    public PowerShellStorageDomainResource(String id,
                                           PowerShellStorageDomainsResource parent,
                                           PowerShellPoolMap shellPools,
                                           PowerShellParser parser) {
        super(id, parent.getExecutor(), parent, shellPools, parser);
        this.parent = parent;
    }

    public static List<StorageDomain> runAndParse(PowerShellPool pool, PowerShellParser parser, String command) {
        List<PowerShellStorageDomain> storageDomains =
            PowerShellStorageDomain.parse(parser, PowerShellCmd.runCommand(pool, command));
        List<StorageDomain> ret = new ArrayList<StorageDomain>();
        for (PowerShellStorageDomain storageDomain : storageDomains) {
            ret.add(storageDomain);
        }
        return ret;
    }

    /**
     * Run a powershell command and parse the output as a single storage
     * domain.
     *
     * @param command the powershell command to execute
     * @param whether the 'sharedStatus' property is needed
     * @return a single storage domain, or null
     */
    public static StorageDomain runAndParseSingle(PowerShellPool pool, PowerShellParser parser, String command) {
        List<StorageDomain> storageDomains = runAndParse(pool, parser, command);

        return !storageDomains.isEmpty() ? storageDomains.get(0) : null;
    }

    public StorageDomain runAndParseSingle(String command) {
        return runAndParseSingle(getPool(), getParser(), command);
    }

    public static StorageDomain addLinks(UriInfo uriInfo, StorageDomain storageDomain) {
        storageDomain = JAXBHelper.clone("storage_domain", StorageDomain.class, storageDomain);

        storageDomain = LinkHelper.addLinks(uriInfo, storageDomain);

        ActionsBuilder actionsBuilder = new ActionsBuilder(LinkHelper.getUriBuilder(uriInfo, storageDomain),
                                                           StorageDomainResource.class);
        storageDomain.setActions(actionsBuilder.build());

        return storageDomain;
    }

    @Override
    public StorageDomain get() {
        return addLinks(getUriInfo(), runAndParseSingle("get-storagedomain " + PowerShellUtils.escape(getId())));
    }

    @Override
    public StorageDomain update(StorageDomain storageDomain) {
        validateUpdate(storageDomain);

        StringBuilder buf = new StringBuilder();

        buf.append("$d = get-storagedomain " + PowerShellUtils.escape(getId()) + ";");

        if (storageDomain.isSetName()) {
            buf.append("$d.name = " + PowerShellUtils.escape(storageDomain.getName()) + ";");
        }

        buf.append("update-storagedomain -storagedomainobject $d");

        return addLinks(getUriInfo(), runAndParseSingle(buf.toString()));
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        return null;
    }

    public StorageDomainContentsResource<VMs, VM> getStorageDomainVmsResource() {
        return null;
    }

    public StorageDomainContentsResource<Templates, Template> getStorageDomainTemplatesResource() {
        return null;
    }
}

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

import java.util.concurrent.Executor;

import javax.ws.rs.core.Response;

import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
import com.redhat.rhevm.api.resource.StorageDomainContentResource;

public class PowerShellStorageDomainTemplateResource
    extends AbstractPowerShellStorageDomainContentResource<Template>
    implements StorageDomainContentResource<Template> {

    public PowerShellStorageDomainTemplateResource(PowerShellStorageDomainTemplatesResource parent,
                                                   String templateId,
                                                   Executor executor,
                                                   UriInfoProvider uriProvider,
                                                   PowerShellPoolMap shellPools,
                                                   PowerShellParser parser) {
        super(parent, templateId, executor, uriProvider, shellPools, parser);
    }

    @Override
    public Template get() {
        StringBuilder buf = new StringBuilder();

        buf.append("get-templateimportcandidates");
        buf.append(" -showall");
        // buf.append(" -datacenterid " + PowerShellUtils.escape(getDataCenterId()));
        buf.append(" -storagedomainid " + PowerShellUtils.escape(getStorageDomainId()));
        buf.append(" | ? { $_.templateid -eq " + PowerShellUtils.escape(getId()) + " }");

        return parent.addLinks(getUriInfo(), parent.runAndParseSingle(buf.toString()));
    }

    @Override
    protected Template getModel() {
        Template template = super.getModel();
        template.setStorageDomain(new StorageDomain());
        template.getStorageDomain().setId(getStorageDomainId());
        return template;
    }

    @Override
    public Response doImport(Action action) {
        return doImport(action, "template");
    }
}

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

import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.common.util.JAXBHelper;
import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.model.Templates;
import com.redhat.rhevm.api.powershell.model.PowerShellTemplate;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
import com.redhat.rhevm.api.resource.StorageDomainContentResource;
import com.redhat.rhevm.api.resource.StorageDomainContentsResource;

public class PowerShellStorageDomainTemplatesResource
    extends AbstractPowerShellStorageDomainContentsResource<Template>
    implements StorageDomainContentsResource<Templates, Template> {

    public PowerShellStorageDomainTemplatesResource(PowerShellStorageDomainResource parent,
                                              PowerShellPoolMap shellPools,
                                              PowerShellParser parser) {
        super(parent, shellPools, parser);
    }

    public List<PowerShellTemplate> runAndParse(String command) {
        return PowerShellTemplate.parse(getParser(), PowerShellCmd.runCommand(getPool(), command));
    }

    @Override
    public Template runAndParseSingle(String command) {
        List<PowerShellTemplate> templates = runAndParse(command);

        return !templates.isEmpty() ? templates.get(0) : null;
    }

    @Override
    public Template addLinks(UriInfo uriInfo, Template template) {
        template = JAXBHelper.clone("template", Template.class, template);

        template.setStorageDomain(new StorageDomain());
        template.getStorageDomain().setId(getStorageDomainId());

        return LinkHelper.addLinks(uriInfo, template);
    }

    @Override
    public Templates list() {
        StringBuilder buf = new StringBuilder();

        buf.append("$sd = get-storagedomain " + PowerShellUtils.escape(getStorageDomainId()));
        buf.append("; ");
        buf.append("if ($sd.domaintype.StartsWith(\"Data\")) { ");
        buf.append("get-template");
        buf.append(" -storagedomainid " + PowerShellUtils.escape(getStorageDomainId()));
        buf.append(" } elseif ($sd.domaintype -eq \"Export\") { ");
        buf.append("get-templateimportcandidates");
        buf.append(" -showall");
        // buf.append(" -datacenterid " + PowerShellUtils.escape(getDataCenterId()));
        buf.append(" -storagedomainid " + PowerShellUtils.escape(getStorageDomainId()));
        buf.append(" }");

        Templates ret = new Templates();
        for (PowerShellTemplate template : runAndParse(buf.toString())) {
            ret.getTemplates().add(addLinks(getUriInfo(), template));
        }
        return ret;
    }

    public StorageDomainContentResource<Template> getStorageDomainContentSubResource(String id) {
        return new PowerShellStorageDomainTemplateResource(this, id, executor, this, shellPools, getParser());
    }
}

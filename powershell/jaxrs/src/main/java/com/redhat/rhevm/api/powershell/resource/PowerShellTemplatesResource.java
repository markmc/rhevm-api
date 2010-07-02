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

import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.model.Templates;
import com.redhat.rhevm.api.resource.TemplateResource;
import com.redhat.rhevm.api.resource.TemplatesResource;
import com.redhat.rhevm.api.powershell.model.PowerShellTemplate;

public class PowerShellTemplatesResource
    extends AbstractPowerShellCollectionResource<Template, PowerShellTemplateResource>
    implements TemplatesResource {

    @Override
    public Templates list(UriInfo uriInfo) {
        Templates ret = new Templates();
        for (PowerShellTemplate template : PowerShellTemplateResource.runAndParse(getShell(), getSelectCommand("select-template", uriInfo, Template.class))) {
            ret.getTemplates().add(PowerShellTemplateResource.addLinks(template));
        }
        return ret;
    }

    @Override
    public TemplateResource getTemplateSubResource(UriInfo uriInfo, String id) {
        return getSubResource(id);
    }

    protected PowerShellTemplateResource createSubResource(String id) {
        return new PowerShellTemplateResource(id, getExecutor(), shellPools);
    }
}

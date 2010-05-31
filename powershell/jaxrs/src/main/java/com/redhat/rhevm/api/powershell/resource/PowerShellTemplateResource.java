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
import java.util.concurrent.Executor;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.resource.TemplateResource;
import com.redhat.rhevm.api.common.resource.AbstractActionableResource;
import com.redhat.rhevm.api.powershell.model.PowerShellTemplate;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;

public class PowerShellTemplateResource extends AbstractActionableResource<Template> implements TemplateResource {

    public PowerShellTemplateResource(String id, Executor executor) {
        super(id, executor);
    }

    public static ArrayList<Template> runAndParse(String command) {
        return PowerShellTemplate.parse(PowerShellCmd.runCommand(command));
    }

    public static Template runAndParseSingle(String command) {
        ArrayList<Template> templates = runAndParse(command);

        return !templates.isEmpty() ? templates.get(0) : null;
    }

    public static Template addLinks(Template template, UriInfo uriInfo, UriBuilder uriBuilder) {
        template.setHref(uriBuilder.build().toString());
        return template;
    }

    @Override
    public Template get(UriInfo uriInfo) {
        StringBuilder buf = new StringBuilder();

        buf.append("get-template -templateid " + getId());

        return addLinks(runAndParseSingle(buf.toString()), uriInfo, uriInfo.getRequestUriBuilder());
    }
}

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

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.CpuTopology;
import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.model.Templates;
import com.redhat.rhevm.api.resource.TemplateResource;
import com.redhat.rhevm.api.resource.TemplatesResource;
import com.redhat.rhevm.api.powershell.model.PowerShellTemplate;
import com.redhat.rhevm.api.powershell.model.PowerShellVM;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;

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
    public Response add(UriInfo uriInfo, Template template) {
        StringBuilder buf = new StringBuilder();

        String vmArg = null;
        if (template.getVm().isSetId()) {
            buf.append("$v = get-vm " + PowerShellUtils.escape(template.getVm().getId())+ "\n");
        } else {
            buf.append("$v = select-vm -searchtext ");
            buf.append(PowerShellUtils.escape("name=" + template.getVm().getName()));
            buf.append("\n");
        }

        String clusterArg = null;
        if (template.getCluster() != null) {
            if (template.getCluster().isSetId()) {
                clusterArg = PowerShellUtils.escape(template.getCluster().getId());
            } else {
                buf.append("$c = select-cluster -searchtext ");
                buf.append(PowerShellUtils.escape("name=" +  template.getCluster().getName()));
                buf.append("\n");
                clusterArg = "$c.ClusterId";
            }
        }

        buf.append("add-template");

        buf.append(" -name " + PowerShellUtils.escape(template.getName()) + "");
        buf.append(" -mastervm $v");
        if (clusterArg != null) {
            buf.append(" -hostclusterid " + clusterArg);
        }

        if (template.getDescription() != null) {
            buf.append(" -description " + PowerShellUtils.escape(template.getDescription()));
        }
        if (template.isSetMemory()) {
            buf.append(" -memory " + Math.round((double)template.getMemory()/(1024*1024)));
        }
        if (template.getCpu() != null && template.getCpu().getTopology() != null) {
            CpuTopology topology = template.getCpu().getTopology();
            buf.append(" -numofsockets " + topology.getSockets());
            buf.append(" -numofcpuspersocket " + topology.getCores());
        }
        String bootSequence = PowerShellVM.buildBootSequence(template.getOs());
        if (bootSequence != null) {
            buf.append(" -defaultbootsequence " + bootSequence);
        }

        PowerShellTemplate ret = PowerShellTemplateResource.runAndParseSingle(getShell(), buf.toString());

        template = PowerShellTemplateResource.addLinks(ret);

        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(ret.getId());

        return Response.created(uriBuilder.build()).entity(template).build();
    }

    @Override
    public void remove(String id) {
        PowerShellCmd.runCommand(getShell(), "remove-template -templateid " + PowerShellUtils.escape(id));
        removeSubResource(id);
    }

    @Override
    public TemplateResource getTemplateSubResource(UriInfo uriInfo, String id) {
        return getSubResource(id);
    }

    protected PowerShellTemplateResource createSubResource(String id) {
        return new PowerShellTemplateResource(id, getExecutor(), shellPools);
    }
}

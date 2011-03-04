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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.redhat.rhevm.api.common.util.ReflectionHelper;
import com.redhat.rhevm.api.model.CpuTopology;
import com.redhat.rhevm.api.model.Display;
import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.model.Templates;
import com.redhat.rhevm.api.resource.TemplateResource;
import com.redhat.rhevm.api.resource.TemplatesResource;
import com.redhat.rhevm.api.powershell.model.PowerShellTemplate;
import com.redhat.rhevm.api.powershell.model.PowerShellVM;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;

import static com.redhat.rhevm.api.common.util.CompletenessAssertor.validateParameters;

public class PowerShellTemplatesResource
    extends AbstractPowerShellCollectionResource<Template, PowerShellTemplateResource>
    implements TemplatesResource {

    static final String PROCESS_TEMPLATES;
    static {
        StringBuilder buf = new StringBuilder();
        buf.append(" | ");
        buf.append("foreach { ");
        buf.append(PowerShellUtils.getDateHack("creationdate"));
        buf.append("$_; ");
        buf.append("};");
        PROCESS_TEMPLATES = buf.toString();
    }

    public List<PowerShellTemplate> runAndParse(String command) {
        return PowerShellTemplateResource.runAndParse(getPool(), getParser(), command);
    }

    public PowerShellTemplate runAndParseSingle(String command) {
        return PowerShellTemplateResource.runAndParseSingle(getPool(), getParser(), command);
    }

    @Override
    public Templates list() {
        Templates ret = new Templates();
        for (PowerShellTemplate template : runAndParse(getSelectCommand("select-template", getUriInfo(), Template.class) + PROCESS_TEMPLATES)) {
            ret.getTemplates().add(PowerShellTemplateResource.addLinks(getUriInfo(), template));
        }
        return ret;
    }

    @Override
    public Response add(Template template) {
        validateParameters(template, "name", "vm.id|name");
        StringBuilder buf = new StringBuilder();
        Response response = null;

        if (template.getVm().isSetId()) {
            buf.append("$v = get-vm " + PowerShellUtils.escape(template.getVm().getId())+ ";");
        } else {
            buf.append("$v = select-vm -searchtext ");
            buf.append(PowerShellUtils.escape("name=" + template.getVm().getName()));
            buf.append(";");
        }

        String clusterArg = null;
        if (template.getCluster() != null) {
            if (template.getCluster().isSetId()) {
                clusterArg = PowerShellUtils.escape(template.getCluster().getId());
            } else {
                buf.append("$c = select-cluster -searchtext ");
                buf.append(PowerShellUtils.escape("name=" +  template.getCluster().getName()));
                buf.append(";");
                clusterArg = "$c.ClusterId";
            }
        }

        buf.append("add-template");
        buf.append(" -name " + PowerShellUtils.escape(template.getName()) + "");
        if (template.getDescription() != null) {
            buf.append(" -description " + PowerShellUtils.escape(template.getDescription()));
        }
        buf.append(" -mastervm $v");
        if (clusterArg != null) {
            buf.append(" -hostclusterid " + clusterArg);
        }
        if (template.getType() != null) {
            buf.append(" -vmtype " + ReflectionHelper.capitalize(template.getType().toString().toLowerCase()));
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
        if (template.isSetOs() && template.getOs().isSetType()) {
            buf.append(" -os " + PowerShellUtils.escape(template.getOs().getType()));
        }
        if (template.isSetStateless()) {
            buf.append(" -stateless ");
        }
        if (template.isSetHighlyAvailable() && template.getHighlyAvailable().isValue()) {
            buf.append(" -highlyavailable ");
        }
        if (template.isSetDisplay()) {
            Display display = template.getDisplay();
            if (display.isSetType()) {
                buf.append(" -displaytype " + PowerShellVM.asString(display.getType()));
            }
        }

        boolean expectBlocking = expectBlocking();
        if (expectBlocking) {
            buf.append(PROCESS_TEMPLATES);
        } else {
            buf.append(ASYNC_OPTION).append(PROCESS_TEMPLATES).append(ASYNC_TASKS);
        }

        PowerShellTemplate created = runAndParseSingle(buf.toString());

        if (expectBlocking || created.getTaskIds() == null) {
            template = PowerShellTemplateResource.addLinks(getUriInfo(), created);
            UriBuilder uriBuilder = getUriInfo().getAbsolutePathBuilder().path(template.getId());
            response = Response.created(uriBuilder.build()).entity(template).build();
        } else {
            template = addStatus(getUriInfo(), PowerShellTemplateResource.addLinks(getUriInfo(), created), created.getTaskIds());
            response = Response.status(202).entity(template).build();
        }

        return response;
    }

    @Override
    public void remove(String id) {
        PowerShellCmd.runCommand(getPool(), "remove-template -templateid " + PowerShellUtils.escape(id));
        removeSubResource(id);
    }

    @Override
    public TemplateResource getTemplateSubResource(String id) {
        return getSubResource(id);
    }

    protected PowerShellTemplateResource createSubResource(String id) {
        return new PowerShellTemplateResource(id, getExecutor(), this, shellPools, getParser(), httpHeaders);
    }

}

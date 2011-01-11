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
import java.util.concurrent.Executor;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.CpuTopology;
import com.redhat.rhevm.api.model.Display;
import com.redhat.rhevm.api.model.Floppy;
import com.redhat.rhevm.api.model.Floppies;
import com.redhat.rhevm.api.model.Link;
import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.resource.AssignedPermissionsResource;
import com.redhat.rhevm.api.resource.CreationResource;
import com.redhat.rhevm.api.resource.ReadOnlyDevicesResource;
import com.redhat.rhevm.api.resource.TemplateResource;
import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.common.util.JAXBHelper;
import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.common.util.ReflectionHelper;
import com.redhat.rhevm.api.powershell.model.PowerShellTemplate;
import com.redhat.rhevm.api.powershell.model.PowerShellVM;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPool;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;

import static com.redhat.rhevm.api.common.util.CompletenessAssertor.validateParameters;
import static com.redhat.rhevm.api.powershell.resource.PowerShellTemplatesResource.PROCESS_TEMPLATES;

public class PowerShellTemplateResource extends AbstractPowerShellActionableResource<Template> implements TemplateResource {

    public PowerShellTemplateResource(String id,
                                      Executor executor,
                                      UriInfoProvider uriProvider,
                                      PowerShellPoolMap shellPools,
                                      PowerShellParser parser,
                                      HttpHeaders httpHeaders) {
        super(id, executor, uriProvider, shellPools, parser);
        setHttpHeaders(httpHeaders);
    }

    public static List<PowerShellTemplate> runAndParse(PowerShellPool pool,
                                                       PowerShellParser parser,
                                                       String command) {
        return PowerShellTemplate.parse(parser, PowerShellCmd.runCommand(pool, command));
    }

    public static PowerShellTemplate runAndParseSingle(PowerShellPool pool, PowerShellParser parser, String command) {
        List<PowerShellTemplate> templates = runAndParse(pool, parser, command);

        return !templates.isEmpty() ? templates.get(0) : null;
    }

    public PowerShellTemplate runAndParseSingle(String command) {
        return runAndParseSingle(getPool(), getParser(), command);
    }

    public static Template addLinks(UriInfo uriInfo, PowerShellTemplate template) {
        Template ret = JAXBHelper.clone("template", Template.class, template);

        String [] deviceCollections = { "cdroms", "disks", "nics" };

        ret.getLinks().clear();

        for (String collection : deviceCollections) {
            Link link = new Link();
            link.setRel(collection);
            link.setHref(LinkHelper.getUriBuilder(uriInfo, ret).path(collection).build().toString());
            ret.getLinks().add(link);
        }

        return LinkHelper.addLinks(uriInfo, ret);
    }

    @Override
    public Template get() {
        StringBuilder buf = new StringBuilder();

        buf.append("get-template -templateid " + PowerShellUtils.escape(getId()));

        return addLinks(getUriInfo(), runAndParseSingle(buf.toString() + PROCESS_TEMPLATES));
    }

    @Override
    public Template update(Template template) {
        validateUpdate(template);

        StringBuilder buf = new StringBuilder();

        buf.append("$t = get-template " + PowerShellUtils.escape(getId()) + ";");

        if (template.getName() != null) {
            buf.append("$t.name = " + PowerShellUtils.escape(template.getName()) + ";");
        }
        if (template.getDescription() != null) {
            buf.append("$t.description = " + PowerShellUtils.escape(template.getDescription()) + ";");
        }
        if (template.getType() != null) {
            buf.append("$t.vmtype = " + ReflectionHelper.capitalize(template.getType().toString().toLowerCase()) + ";");
        }
        if (template.isSetMemory()) {
            buf.append(" $t.memsizemb = " + Math.round((double)template.getMemory()/(1024*1024)) + ";");
        }
        if (template.getCpu() != null && template.getCpu().getTopology() != null) {
            CpuTopology topology = template.getCpu().getTopology();
            if (topology.isSetSockets()) {
                buf.append(" $t.numofsockets = " + topology.getSockets() + ";");
            }
            if (topology.isSetCores()) {
                buf.append(" $t.numofcpuspersocket = " + topology.getCores() + ";");
            }
        }
        String bootSequence = PowerShellVM.buildBootSequence(template.getOs());
        if (bootSequence != null) {
            buf.append(" $t.defaultbootsequence = '" + bootSequence + "';");
        }
        if (template.isSetOs() && template.getOs().isSetType()) {
            buf.append(" $t.operatingsystem = " + PowerShellUtils.escape(template.getOs().getType()) + ";");
        }
        if (template.isSetStateless()) {
            buf.append(" $t.isstateless = " + PowerShellUtils.encode(template.isStateless()) + ";");
        }
        if (template.isSetHighlyAvailable()) {
            buf.append(" $t.autostartup = " + PowerShellUtils.encode(template.getHighlyAvailable().isValue()) + ";");
        }
        if (template.isSetDisplay()) {
            Display display = template.getDisplay();
            if (display.isSetType()) {
                buf.append(" $t.displaytype = '" + PowerShellVM.asString(display.getType()) + "';");
            }
        }

        buf.append("update-template -templateobject $t");

        return addLinks(getUriInfo(), runAndParseSingle(buf.toString() + PROCESS_TEMPLATES));
    }

    @Override
    public Response export(Action action) {
        validateParameters(action, "storageDomain.id|name");

        StringBuilder buf = new StringBuilder();

        String storageDomainArg;
        if (action.getStorageDomain().isSetId()) {
            storageDomainArg = PowerShellUtils.escape(action.getStorageDomain().getId());
        } else {
            buf.append("$dest = select-storagedomain ");
            buf.append("| ? { $_.name -eq ");
            buf.append(PowerShellUtils.escape(action.getStorageDomain().getName()));
            buf.append(" }; ");
            storageDomainArg = "$dest.storagedomainid";
        }

        buf.append("export-template");
        buf.append(" -templateid " + PowerShellUtils.escape(getId()));
        buf.append(" -storagedomainid " + storageDomainArg);

        if (!action.isSetExclusive() || !action.isExclusive()) {
            buf.append(" -forceoverride");
        }

        return doAction(getUriInfo(), new CommandRunner(action, buf.toString(), getPool()));
    }

    public class CdRomQuery extends PowerShellCdRomsResource.CdRomQuery {
        public CdRomQuery(String id) {
            super(id);
        }
        @Override protected String getCdIsoPath() {
            return runAndParseSingle("get-template " + PowerShellUtils.escape(id)).getCdIsoPath();
        }
    }

    @Override
    public PowerShellReadOnlyCdRomsResource getCdRomsResource() {
        return new PowerShellReadOnlyCdRomsResource(getId(), shellPools, new CdRomQuery(getId()), getUriProvider());
    }

    @Override
    public PowerShellReadOnlyDisksResource getDisksResource() {
        return new PowerShellReadOnlyDisksResource(getId(), shellPools, getParser(), "get-template", getUriProvider());
    }

    @Override
    public ReadOnlyDevicesResource<Floppy, Floppies> getFloppiesResource() {
        // Not available in powershell, so we cannot support this
        return null;
    }

    @Override
    public PowerShellReadOnlyNicsResource getNicsResource() {
        return new PowerShellReadOnlyNicsResource(getId(), shellPools, getParser(), "get-template", getUriProvider());
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        return null;
    }

    @Override
    public CreationResource getCreationSubresource(String ids) {
        // REVISIT
        return null;
    }
}

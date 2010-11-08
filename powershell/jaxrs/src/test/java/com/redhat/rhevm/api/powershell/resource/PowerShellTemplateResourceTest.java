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

import java.net.URI;
import java.text.MessageFormat;
import java.util.concurrent.Executor;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.Display;
import com.redhat.rhevm.api.model.DisplayType;
import com.redhat.rhevm.api.model.Fault;
import com.redhat.rhevm.api.model.HighlyAvailable;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.Template;

import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;

import org.junit.Test;

import static org.easymock.classextension.EasyMock.expect;

import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;

import static com.redhat.rhevm.api.powershell.resource.PowerShellTemplatesResource.PROCESS_TEMPLATES;

public class PowerShellTemplateResourceTest extends AbstractPowerShellResourceTest<Template, PowerShellTemplateResource> {

    private static final String TEMPLATE_NAME = "sedna";
    private static final String TEMPLATE_ID = Integer.toString(TEMPLATE_NAME.hashCode());
    private static final String TEMPLATE_DESCRIPTION = "this is a template";
    private static final String NEW_NAME = "eris";
    private static final String STORAGE_DOMAIN_NAME = "xtratime";
    private static final String STORAGE_DOMAIN_ID = Integer.toString(STORAGE_DOMAIN_NAME.hashCode());
    private static final String BAD_ID = "98765";

    private static final String GET_COMMAND = "get-template -templateid \"" + TEMPLATE_ID + "\"" + PROCESS_TEMPLATES;
    private static final String UPDATE_COMMAND_TEMPLATE = "$t = get-template \"" + TEMPLATE_ID + "\";$t.name = \"" + NEW_NAME + "\";{0}update-template -templateobject $t";
    private static final String UPDATE_COMMAND = MessageFormat.format(UPDATE_COMMAND_TEMPLATE, "") + PROCESS_TEMPLATES;
    private static final String UPDATE_HIGHLY_AVAILABLE_COMMAND = MessageFormat.format(UPDATE_COMMAND_TEMPLATE, " $t.autostartup = $false;") + PROCESS_TEMPLATES;
    private static final String UPDATE_STATELESS_COMMAND = MessageFormat.format(UPDATE_COMMAND_TEMPLATE, " $t.isstateless = $true;")  + PROCESS_TEMPLATES;
    private static final String UPDATE_DISPLAY_COMMAND = MessageFormat.format(UPDATE_COMMAND_TEMPLATE, " $t.numofmonitors = 4; $t.displaytype = 'VNC';")  + PROCESS_TEMPLATES;

    private static final String EXPORT_COMMAND = "$dest = select-storagedomain | ? { $_.domaintype -eq \"Export\" }; export-template -templateid \"" + TEMPLATE_ID + "\" -storagedomainid $dest.storagedomainid -forceoverride";
    private static final String EXPORT_WITH_PARAMS_COMMAND = "$dest = select-storagedomain | ? { $_.domaintype -eq \"Export\" }; export-template -templateid \"" + TEMPLATE_ID + "\" -storagedomainid $dest.storagedomainid";
    private static final String EXPORT_WITH_STORAGE_DOMAIN_COMMAND = "export-template -templateid \"" + TEMPLATE_ID + "\" -storagedomainid \"" + STORAGE_DOMAIN_ID + "\" -forceoverride";
    private static final String EXPORT_WITH_NAMED_STORAGE_DOMAIN_COMMAND = "$dest = select-storagedomain | ? { $_.name -eq \"" + STORAGE_DOMAIN_NAME + "\" }; export-template -templateid \"" + TEMPLATE_ID + "\" -storagedomainid $dest.storagedomainid -forceoverride";

    protected PowerShellTemplateResource getResource(Executor executor, PowerShellPoolMap poolMap, PowerShellParser parser, UriInfoProvider uriProvider) {
        return new PowerShellTemplateResource(TEMPLATE_ID, executor, uriProvider, poolMap, parser);
    }

    protected String formatTemplate(String name) {
        return formatXmlReturn("template",
                               new String[] { name },
                               new String[] { TEMPLATE_DESCRIPTION },
                               PowerShellTemplatesResourceTest.extraArgs);
    }

    @Test
    public void testGet() throws Exception {
        setUriInfo(setUpTemplateExpectations(GET_COMMAND, formatTemplate(TEMPLATE_NAME)));
        verifyTemplate(resource.get(), TEMPLATE_NAME);
    }

    @Test
    public void testGoodUpdate() throws Exception {
        setUriInfo(setUpTemplateExpectations(UPDATE_COMMAND, formatTemplate(NEW_NAME)));
        verifyTemplate(resource.update(getTemplate(NEW_NAME)), NEW_NAME);
    }

    @Test
    public void testUpdateHighlyAvailable() throws Exception {
        setUriInfo(setUpTemplateExpectations(UPDATE_HIGHLY_AVAILABLE_COMMAND, formatTemplate(NEW_NAME)));
        verifyTemplate(resource.update(updateHighlyAvailable(getTemplate(NEW_NAME))), NEW_NAME);
    }

    @Test
    public void testUpdateStateless() throws Exception {
        setUriInfo(setUpTemplateExpectations(UPDATE_STATELESS_COMMAND, formatTemplate(NEW_NAME)));
        verifyTemplate(resource.update(updateStateless(getTemplate(NEW_NAME))), NEW_NAME);
    }

    @Test
    public void testUpdateDisplay() throws Exception {
        setUriInfo(setUpTemplateExpectations(UPDATE_DISPLAY_COMMAND, formatTemplate(NEW_NAME)));
        verifyTemplate(resource.update(updateDisplay(getTemplate(NEW_NAME))), NEW_NAME);
    }

    @Test
    public void testBadUpdate() throws Exception {
        try {
            setUriInfo(createMock(UriInfo.class));
            replayAll();
            resource.update(getTemplate(BAD_ID, NEW_NAME));
            fail("expected WebApplicationException on bad update");
        } catch (WebApplicationException wae) {
            verifyUpdateException(wae);
        }
    }

    @Test
    public void testExport() throws Exception {
        Action action = getAction();
        setUriInfo(setUpActionExpectation("export", EXPORT_COMMAND));
        verifyActionResponse(resource.export(action));
    }

    @Test
    public void testExportWithParams() throws Exception {
        Action action = getAction();
        action.setExclusive(true);
        setUriInfo(setUpActionExpectation("export", EXPORT_WITH_PARAMS_COMMAND));
        verifyActionResponse(resource.export(action));
    }

    @Test
    public void testExportWithStorageDomain() throws Exception {
        Action action = getAction();
        action.setStorageDomain(new StorageDomain());
        action.getStorageDomain().setId(STORAGE_DOMAIN_ID);
        setUriInfo(setUpActionExpectation("export", EXPORT_WITH_STORAGE_DOMAIN_COMMAND));
        verifyActionResponse(resource.export(action));
    }

    @Test
    public void testExportWithNamedStorageDomain() throws Exception {
        Action action = getAction();
        action.setStorageDomain(new StorageDomain());
        action.getStorageDomain().setName(STORAGE_DOMAIN_NAME);
        setUriInfo(setUpActionExpectation("export", EXPORT_WITH_NAMED_STORAGE_DOMAIN_COMMAND));
        verifyActionResponse(resource.export(action));
    }

    private UriInfo setUpActionExpectation(String verb, String command) throws Exception {
        return setUpActionExpectation("/templates/" + TEMPLATE_ID + "/", verb, command, null);
    }

    private void verifyActionResponse(Response r) throws Exception {
        verifyActionResponse(r, "templates/" + TEMPLATE_ID, false);
    }

    private UriInfo setUpTemplateExpectations(String command, String ret) throws Exception {
        mockStatic(PowerShellCmd.class);
        expect(PowerShellCmd.runCommand(setUpPoolExpectations(), command)).andReturn(ret);
        UriInfo uriInfo = setUpBasicUriExpectations();
        UriBuilder uriBuilder = createMock(UriBuilder.class);
        expect(uriInfo.getRequestUriBuilder()).andReturn(uriBuilder).anyTimes();
        expect(uriBuilder.build()).andReturn(new URI("templates/" + TEMPLATE_ID)).anyTimes();
        replayAll();

        return uriInfo;
    }

    private Template getTemplate(String name) {
        return getTemplate(TEMPLATE_ID, name);
    }

    private Template getTemplate(String id, String name) {
        Template template = new Template();
        template.setId(id);
        template.setName(name);
        return template;
    }

    private Template updateHighlyAvailable(Template template) {
        template.setHighlyAvailable(new HighlyAvailable());
        template.getHighlyAvailable().setValue(false);
        return template;
    }

    private Template updateStateless(Template template) {
        template.setStateless(true);
        return template;
    }

    private Template updateDisplay(Template template) {
        template.setDisplay(new Display());
        template.getDisplay().setType(DisplayType.VNC);
        template.getDisplay().setMonitors(4);
        return template;
    }

    private void verifyTemplate(Template template, String name) {
        assertNotNull(template);
        assertEquals(template.getId(), Integer.toString(name.hashCode()));
        assertEquals(template.getName(), name);
        assertEquals(template.getDescription(), TEMPLATE_DESCRIPTION);
        verifyLinks(template);
    }

    private void verifyUpdateException(WebApplicationException wae) {
        assertEquals(409, wae.getResponse().getStatus());
        Fault fault = (Fault)wae.getResponse().getEntity();
        assertNotNull(fault);
        assertEquals("Broken immutability constraint", fault.getReason());
        assertEquals("Attempt to set immutable field: id", fault.getDetail());
    }
}

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

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Template;

import com.redhat.rhevm.api.powershell.util.PowerShellCmd;

import org.junit.Test;

import static org.easymock.classextension.EasyMock.expect;

import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;


public class PowerShellTemplateResourceTest extends AbstractPowerShellResourceTest<Template, PowerShellTemplateResource> {

    private static final String TEMPLATE_ID = "12345";
    private static final String TEMPLATE_NAME = "sedna";
    private static final String TEMPLATE_DESCRIPTION = "this is a template";
    private static final String GET_COMMAND = "get-template -templateid " + TEMPLATE_ID;
    private static final String GET_RETURN = "templateid: " + TEMPLATE_ID + "\nname: " + TEMPLATE_NAME + "\ndescription: " + TEMPLATE_DESCRIPTION;

    protected PowerShellTemplateResource getResource() {
        return new PowerShellTemplateResource(TEMPLATE_ID, executor);
    }

    @Test
    public void testGet() throws Exception {
        verifyTemplate(resource.get(setUpTemplateExpectations(GET_COMMAND, GET_RETURN)));
    }

    private UriInfo setUpTemplateExpectations(String command, String ret) throws Exception {
        mockStatic(PowerShellCmd.class);
        expect(PowerShellCmd.runCommand(command)).andReturn(ret);
        UriInfo uriInfo = createMock(UriInfo.class);
        UriBuilder uriBuilder = createMock(UriBuilder.class);
        expect(uriInfo.getRequestUriBuilder()).andReturn(uriBuilder).anyTimes();
        expect(uriBuilder.build()).andReturn(new URI(URI_ROOT + "/templates/" + TEMPLATE_ID)).anyTimes();
        replayAll();

        return uriInfo;
    }

    private void verifyTemplate(Template template) {
        assertNotNull(template);
        assertEquals(template.getId(), TEMPLATE_ID);
        assertEquals(template.getName(), TEMPLATE_NAME);
        assertEquals(template.getDescription(), TEMPLATE_DESCRIPTION);
    }
}

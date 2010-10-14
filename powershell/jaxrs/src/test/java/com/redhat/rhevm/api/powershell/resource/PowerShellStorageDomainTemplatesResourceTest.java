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

import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.model.Templates;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;

import org.junit.Test;

import static org.powermock.api.easymock.PowerMock.replayAll;

public class PowerShellStorageDomainTemplatesResourceTest
    extends AbstractPowerShellStorageDomainContentsResourceTest<Templates, Template, PowerShellStorageDomainTemplatesResource> {

    protected static final String COLLECTION_URI = STORAGE_DOMAIN_URI + SLASH + "templates";

    private static final String GET_TEMPLATES_COMMAND = "$sd = get-storagedomain \"" + STORAGE_DOMAIN_ID + "\"; if ($sd.domaintype -eq \"Data\") { get-template -storagedomainid \"" + STORAGE_DOMAIN_ID + "\" } elseif ($sd.domaintype -eq \"Export\") { get-templateimportcandidates -showall -datacenterid \"" + DATA_CENTER_ID + "\" -storagedomainid \"" + STORAGE_DOMAIN_ID + "\" }";

    private static final String GET_TEMPLATE_COMMAND = "$sd = get-storagedomain \"" + STORAGE_DOMAIN_ID + "\"; if ($sd.domaintype -eq \"Data\") { get-template -templateid \"" + asId(NAMES[0]) + "\" } elseif ($sd.domaintype -eq \"Export\") { get-templateimportcandidates -showall -datacenterid \"" + DATA_CENTER_ID + "\" -storagedomainid \"" + STORAGE_DOMAIN_ID + "\" | ? { $_.templateid -eq \"" + asId(NAMES[0]) + "\" } }";

    protected PowerShellStorageDomainTemplatesResource getResource(PowerShellAttachedStorageDomainResource parent,
                                                                   PowerShellPoolMap poolMap,
                                                                   PowerShellParser parser) {
        return new PowerShellStorageDomainTemplatesResource(parent, poolMap, parser);
    }

    protected String formatTemplates(String[] names) {
        String[] descriptions = new String[names.length];
        return formatXmlReturn("template", names, descriptions, PowerShellTemplatesResourceTest.extraArgs);
    }

    protected String formatTemplate(String name) {
        return formatTemplates(asArray(name));
    }

    @Test
    public void testGetList() {
        setUpCmdExpectations(GET_TEMPLATES_COMMAND, formatTemplates(NAMES));
        setUriInfo(setUpBasicUriExpectations());
        replayAll();
        verifyTemplates(resource.list());
    }

    @Test
    public void testGet() {
        PowerShellStorageDomainTemplateResource childResource = new
            PowerShellStorageDomainTemplateResource(resource,
                                                    asId(NAMES[0]),
                                                    executor,
                                                    uriProvider,
                                                    poolMap,
                                                    parser);
        setUpCmdExpectations(GET_TEMPLATE_COMMAND, formatTemplate(NAMES[0]));
        setUriInfo(setUpBasicUriExpectations());
        replayAll();
        verifyTemplate(childResource.get(), 0);
    }

    protected void verifyTemplates(Templates templates) {
        assertNotNull(templates.getTemplates());
        assertEquals("unexpected collection size", templates.getTemplates().size(), NAMES.length);
        for (int i = 0; i < NAMES.length; i++) {
            verifyTemplate(templates.getTemplates().remove(0), i);
        }
    }

    protected void verifyTemplate(Template template, int index) {
        assertEquals(asId(NAMES[index]), template.getId());
        assertEquals(NAMES[index], template.getName());
        assertEquals(COLLECTION_URI + SLASH + asId(NAMES[index]), template.getHref());
        assertNotNull(template.getStorageDomain());
        assertEquals(STORAGE_DOMAIN_ID, template.getStorageDomain().getId());
        assertEquals(STORAGE_DOMAIN_URI, template.getStorageDomain().getHref());
        assertFalse(template.getStorageDomain().isSetDataCenter());
    }
}

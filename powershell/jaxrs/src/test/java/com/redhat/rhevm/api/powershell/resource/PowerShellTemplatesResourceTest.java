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

import org.junit.Test;

public class PowerShellTemplatesResourceTest extends AbstractPowerShellCollectionResourceTest<Template, PowerShellTemplateResource, PowerShellTemplatesResource> {

    public PowerShellTemplatesResourceTest() {
        super(new PowerShellTemplateResource("0", null), "templates", "template");
    }

    @Test
    public void testList() throws Exception {
        verifyCollection(
            resource.list(setUpResourceExpectations("select-template",
                                                    getSelectReturn(),
                                                    NAMES)).getTemplates(),
            NAMES);
    }

    @Test
    public void testGetSubResource() throws Exception {
        verifyResource(
            (PowerShellTemplateResource)resource.getTemplateSubResource(setUpResourceExpectations(null, null),
                                                                        Integer.toString(NEW_NAME.hashCode())),
            NEW_NAME);
    }

    protected PowerShellTemplatesResource getResource() {
        return new PowerShellTemplatesResource();
    }

    protected void populateModel(Template template) {
    }
}

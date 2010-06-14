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
package com.redhat.rhevm.api.mock.resource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.mock.util.SimpleQueryEvaluator;
import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.model.Templates;
import com.redhat.rhevm.api.resource.TemplateResource;
import com.redhat.rhevm.api.resource.TemplatesResource;

import static com.redhat.rhevm.api.mock.resource.AbstractMockResource.allocateId;


public class MockTemplatesResource extends AbstractMockQueryableResource<Template> implements TemplatesResource {

    private static Map<String, MockTemplateResource> templates =
        Collections.synchronizedMap(new HashMap<String, MockTemplateResource>());

    public MockTemplatesResource() {
        super(new SimpleQueryEvaluator<Template>());
    }

    public void populate() {
        synchronized (templates) {
            while (templates.size() < 4) {
                MockTemplateResource resource = new MockTemplateResource(allocateId(Template.class), getExecutor());
                resource.getModel().setName("template" + resource.getModel().getId());
                templates.put(resource.getModel().getId(), resource);
            }
        }
    }

    @Override
    public Templates list(UriInfo uriInfo) {
        Templates ret = new Templates();

        for (MockTemplateResource template : templates.values()) {
            if (filter(template.getModel(), uriInfo, Template.class)) {
                ret.getTemplates().add(template.addLinks());
            }
        }

        return ret;
    }

    @Override
    public TemplateResource getTemplateSubResource(UriInfo uriInfo, String id) {
        return templates.get(id);
    }
}

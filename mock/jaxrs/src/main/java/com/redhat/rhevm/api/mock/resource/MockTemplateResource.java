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

import java.util.concurrent.Executor;

import javax.ws.rs.core.Response;

import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.resource.DevicesResource;
import com.redhat.rhevm.api.resource.TemplateResource;
import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.common.util.JAXBHelper;
import com.redhat.rhevm.api.common.util.LinkHelper;


public class MockTemplateResource extends AbstractMockResource<Template> implements TemplateResource {

    /**
     * Package-protected ctor, never needs to be instantiated by JAX-RS framework.
     *
     * @param template     encapsulated template
     * @param executor executor used for asynchronous actions
     */
    MockTemplateResource(String id, Executor executor, UriInfoProvider uriProvider) {
        super(id, executor, uriProvider);
    }

    // FIXME: this needs to be atomic
    public void updateModel(Template template) {
        // update writable fields only
        if (template.isSetName()) {
            getModel().setName(template.getName());
        }
        if (template.isSetDescription()) {
            getModel().setDescription(template.getDescription());
        }
    }

    public Template addLinks() {
        return LinkHelper.addLinks(getUriInfo(), JAXBHelper.clone("template", Template.class, getModel()));
    }

    @Override
    public Template get() {
        return addLinks();
    }

    @Override
    public Template update(Template template) {
        validateUpdate(template);
        updateModel(template);
        return addLinks();
    }

    @Override
    public Response export(Action action) {
        return null;
    }

    @Override public DevicesResource getCdRomsResource() { return null; }
    @Override public DevicesResource getDisksResource()  { return null; }
    @Override public DevicesResource getNicsResource()   { return null; }
}

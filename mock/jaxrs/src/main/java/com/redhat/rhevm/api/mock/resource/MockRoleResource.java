/*
 * Copyright © 2011 Red Hat, Inc.
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

import com.redhat.rhevm.api.model.Role;
import com.redhat.rhevm.api.resource.PermitsResource;
import com.redhat.rhevm.api.resource.RoleResource;
import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.common.util.JAXBHelper;
import com.redhat.rhevm.api.common.util.LinkHelper;


public class MockRoleResource extends AbstractMockResource<Role> implements RoleResource {
    /* FIXME: would like to do:
     * private @Context UriInfo uriInfo;
     */

    /**
     * Package-protected ctor, never needs to be instantiated by JAX-RS framework.
     *
     * @param role     encapsulated role
     * @param executor executor used for asynchronous actions
     */
    MockRoleResource(String id, Executor executor, UriInfoProvider uriProvider) {
        super(id, executor, uriProvider);
    }

    // FIXME: this needs to be atomic
    public void updateModel(Role role) {
        // update writable fields only
        if (role.isSetName()) {
            getModel().setName(role.getName());
        }
        if (role.isSetDescription()) {
            getModel().setDescription(role.getDescription());
        }
    }

    public Role addLinks() {
        return LinkHelper.addLinks(getUriInfo(), JAXBHelper.clone(OBJECT_FACTORY.createRole(getModel())));
    }

    /* FIXME: kill uriInfo param, make href auto-generated? */
    @Override
    public Role get() {
        return addLinks();
    }

    @Override
    public PermitsResource getPermitsResource() {
        // TODO Auto-generated method stub
        return null;
    }
}

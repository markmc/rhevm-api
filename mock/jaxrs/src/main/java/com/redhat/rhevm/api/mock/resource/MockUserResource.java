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

import com.redhat.rhevm.api.model.User;
import com.redhat.rhevm.api.resource.AssignedPermissionsResource;
import com.redhat.rhevm.api.resource.AssignedRolesResource;
import com.redhat.rhevm.api.resource.AssignedTagsResource;
import com.redhat.rhevm.api.resource.UserResource;
import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.common.util.JAXBHelper;
import com.redhat.rhevm.api.common.util.LinkHelper;


public class MockUserResource extends AbstractMockResource<User> implements UserResource {
    /* FIXME: would like to do:
     * private @Context UriInfo uriInfo;
     */

    /**
     * Package-protected ctor, never needs to be instantiated by JAX-RS framework.
     *
     * @param user     encapsulated user
     * @param executor executor used for asynchronous actions
     */
    MockUserResource(String id, Executor executor, UriInfoProvider uriProvider) {
        super(id, executor, uriProvider);
    }

    // FIXME: this needs to be atomic
    public void updateModel(User user) {
        // update writable fields only
        if (user.isSetName()) {
            getModel().setName(user.getName());
        }
        if (user.isSetUserName()) {
            getModel().setUserName(user.getUserName());
        }
    }

    public User addLinks() {
        return LinkHelper.addLinks(getUriInfo(), JAXBHelper.clone(OBJECT_FACTORY.createUser(getModel())));
    }

    /* FIXME: kill uriInfo param, make href auto-generated? */
    @Override
    public User get() {
        return addLinks();
    }

    @Override
    public AssignedRolesResource getRolesResource() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AssignedTagsResource getTagsResource() {
        // TODO Auto-generated method stub
        return null;
    }
}

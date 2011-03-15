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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.mock.util.SimpleQueryEvaluator;
import com.redhat.rhevm.api.model.User;
import com.redhat.rhevm.api.model.Users;
import com.redhat.rhevm.api.resource.UserResource;
import com.redhat.rhevm.api.resource.UsersResource;

import static com.redhat.rhevm.api.mock.resource.AbstractMockResource.allocateId;


public class MockUsersResource extends AbstractMockQueryableResource<User> implements UsersResource {

    private static Map<String, MockUserResource> users =
        Collections.synchronizedMap(new HashMap<String, MockUserResource>());

    public MockUsersResource() {
        super(new SimpleQueryEvaluator<User>());
    }

    public void populate() {
        synchronized (users) {
            while (users.size() < 2) {
                MockUserResource resource = new MockUserResource(allocateId(User.class), getExecutor(), this);
                User user = resource.getModel();
                user.setName("user" + resource.getModel().getId());
                user.setUserName("user_" + resource.getModel().getId());
                users.put(user.getId(), resource);
            }
        }
    }

    @Override
    public Users list() {
        Users ret = new Users();

        for (MockUserResource user : users.values()) {
            UriInfo info = getUriInfo();
            if (filter(user.getModel(), info, User.class)) {
                ret.getUsers().add(user.addLinks());
            }
        }

        return ret;
    }

    @Override
    public Response add(User user) {
        MockUserResource resource = new MockUserResource(allocateId(User.class), getExecutor(), this);

        resource.updateModel(user);

        String id = resource.getId();
        users.put(id, resource);

        UriBuilder uriBuilder = getUriInfo().getAbsolutePathBuilder().path(id);

        user = resource.addLinks();

        return Response.created(uriBuilder.build()).entity(user).build();
    }

    @Override
    public void remove(String id) {
        users.remove(id);
    }

    @Override
    public UserResource getUserSubResource(String id) {
        return users.get(id);
    }
}

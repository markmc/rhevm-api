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
import com.redhat.rhevm.api.model.Role;
import com.redhat.rhevm.api.model.Roles;
import com.redhat.rhevm.api.resource.RoleResource;
import com.redhat.rhevm.api.resource.RolesResource;

import static com.redhat.rhevm.api.mock.resource.AbstractMockResource.allocateId;


public class MockRolesResource extends AbstractMockQueryableResource<Role> implements RolesResource {

    private static Map<String, MockRoleResource> roles =
        Collections.synchronizedMap(new HashMap<String, MockRoleResource>());

    public MockRolesResource() {
        super(new SimpleQueryEvaluator<Role>());
    }

    public void populate() {
        synchronized (roles) {
            while (roles.size() < 2) {
                MockRoleResource resource = new MockRoleResource(allocateId(Role.class), getExecutor(), this);
                Role role = resource.getModel();
                role.setName("role" + resource.getModel().getId());
                role.setDescription("test role");
                roles.put(role.getId(), resource);
            }
        }
    }

    @Override
    public Roles list() {
        Roles ret = new Roles();

        for (MockRoleResource role : roles.values()) {
            UriInfo info = getUriInfo();
            if (filter(role.getModel(), info, Role.class)) {
                ret.getRoles().add(role.addLinks());
            }
        }

        return ret;
    }

    @Override
    public Response add(Role role) {
        MockRoleResource resource = new MockRoleResource(allocateId(Role.class), getExecutor(), this);

        resource.updateModel(role);

        String id = resource.getId();
        roles.put(id, resource);

        UriBuilder uriBuilder = getUriInfo().getAbsolutePathBuilder().path(id);

        role = resource.addLinks();

        return Response.created(uriBuilder.build()).entity(role).build();
    }

    @Override
    public void remove(String id) {
        roles.remove(id);
    }

    @Override
    public RoleResource getRoleSubResource(String id) {
         return roles.get(id);
    }
}

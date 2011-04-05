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

import org.junit.Test;

import com.redhat.rhevm.api.model.User;
import com.redhat.rhevm.api.model.Users;

public class MockUserResourceTest extends MockTestBase {
    private MockTestBase.UsersResource getService() {
        return createUsersResource(getEntryPoint("users").getHref());
    }

    private void checkUser(User user) {
        assertNotNull(user.getName());
        assertNotNull(user.getId());
        assertNotNull(user.getHref());
        assertTrue(user.getHref().endsWith("users/" + user.getId()));
        assertNull(user.getActions());
    }

    @Test
    public void testGetUsersList() throws Exception {
        MockTestBase.UsersResource service = getService();
        assertNotNull(service);

        Users users = service.list(null);
        assertNotNull(users);
        assertTrue(users.getUsers().size() > 0);

        for (User user : users.getUsers()) {
            checkUser(user);

            User t = service.get(user.getId());
            checkUser(t);
            assertEquals(user.getId(), t.getId());
        }
    }

    @Test
    public void testGetUsersQuery() throws Exception {
        MockTestBase.UsersResource service = getService();
        assertNotNull(service);

        Users users = service.list("name=*1");
        assertNotNull(users);
        assertEquals("unepected number of query matches", 1, users.getUsers().size());

        User user = users.getUsers().get(0);
        checkUser(user);

        User t = service.get(user.getId());
        checkUser(t);
        assertEquals(user.getId(), t.getId());
    }

    @Test
    public void testAddUser() throws Exception {
        MockTestBase.UsersResource service = getService();
        assertNotNull(service);

        Users users = service.list(null);
        assertNotNull(users);
        assertTrue(users.getUsers().size() > 0);

        User user = new User();
        Integer id = users.getUsers().size() + 1;
        user.setName("user" + id);
        user.setUserName("user_" +id);

        User result = service.add(user);
        assertNotNull(result);
        checkUser(result);
        assertEquals(id.toString(), result.getId());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getUserName(), result.getUserName());
    }

    @Test
    public void testRemoveUsers() throws Exception {
        MockTestBase.UsersResource service = getService();
        assertNotNull(service);

        Users users = service.list(null);
        assertNotNull(users);
        assertTrue(users.getUsers().size() > 0);

        for (User user : users.getUsers()) {
            service.remove(user.getId(), new User());
        }

        users = service.list(null);
        assertNotNull(users);
        assertTrue(users.getUsers().size() == 0);
    }
}

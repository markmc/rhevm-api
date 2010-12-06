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
package com.redhat.rhevm.api.command.users;

import com.redhat.rhevm.api.model.Group;
import com.redhat.rhevm.api.model.User;

import com.redhat.rhevm.api.command.base.VerboseDisplay;

public class UserVerboseDisplay implements VerboseDisplay<User> {
    @Override
    public void expand(User model) {
        if (model.isSetUserName()) {
            System.out.println("  user name: " + model.getUserName());
        }
        if (model.isSetLastName()) {
            System.out.println("  last name: " + model.getLastName());
        }
        if (model.isSetDomain()) {
            System.out.println("  domain: " + model.getDomain());
        }
        if (model.isSetEmail()) {
            System.out.println("  email: " + model.getEmail());
        }
        if (model.isSetDepartment()) {
            System.out.println("  department: " + model.getDepartment());
        }
        if (model.isSetLoggedIn()) {
            System.out.println("  logged in: " + model.isLoggedIn());
        }
        if (model.isSetGroups()) {
            String prefix = "  groups: ";
            for (Group group : model.getGroups().getGroups()) {
                System.out.println(prefix + group.getName());
                prefix = "          ";
            }
        }
    }
}

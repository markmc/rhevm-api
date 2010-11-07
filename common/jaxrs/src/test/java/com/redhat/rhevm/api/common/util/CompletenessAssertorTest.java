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
package com.redhat.rhevm.api.common.util;

import java.util.Arrays;

import javax.ws.rs.WebApplicationException;

import com.redhat.rhevm.api.model.Fault;
import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.model.Permission;
import com.redhat.rhevm.api.model.Role;
import com.redhat.rhevm.api.model.Roles;
import com.redhat.rhevm.api.model.User;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.model.VmStatus;

import org.junit.Assert;
import org.junit.Test;

public class CompletenessAssertorTest extends Assert {

    @Test
    public void testMissingParameter() throws Exception {
        VM vm = new VM();
        vm.setDescription("incomplete");
        try {
            CompletenessAssertor.validateParameters(vm, "status");
            fail("expected WebApplicationException on incomplete model");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "VM", "status");
        }
    }

    @Test
    public void testMissingParameters() throws Exception {
        VM vm = new VM();
        vm.setDescription("incomplete");
        try {
            CompletenessAssertor.validateParameters(vm, "name", "host", "status");
            fail("expected WebApplicationException on incomplete model");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "VM", "name", "host", "status");
        }
    }

    @Test
    public void testMissingParameterAlteratives() throws Exception {
        VM vm = new VM();
        vm.setDescription("incomplete");
        try {
            CompletenessAssertor.validateParameters(vm, "status|host|name");
            fail("expected WebApplicationException on incomplete model");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "VM", "status|host|name");
        }
    }

    @Test
    public void testCompleteParameters() throws Exception {
        VM vm = new VM();
        vm.setName("foo");
        vm.setStatus(VmStatus.UP);
        vm.setHost(new Host());
        CompletenessAssertor.validateParameters(vm, "name", "host", "status");
    }

    @Test
    public void testCompleteParametersAlternativesFirst() throws Exception {
        VM vm = new VM();
        vm.setName("foo");
        vm.setHost(new Host());
        CompletenessAssertor.validateParameters(vm, "name", "host|status");
    }

    @Test
    public void testCompleteParametersAlternativesSecond() throws Exception {
        VM vm = new VM();
        vm.setName("foo");
        vm.setStatus(VmStatus.UP);
        CompletenessAssertor.validateParameters(vm, "name", "host|status");
    }

    @Test
    public void testMissingSuperField() throws Exception {
        VM vm = new VM();
        vm.setDescription("incomplete");
        try {
            CompletenessAssertor.validateParameters(vm, "host.id");
            fail("expected WebApplicationException on incomplete model");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "VM", "host.id");
        }
    }

    @Test
    public void testMissingSubField() throws Exception {
        VM vm = new VM();
        vm.setDescription("incomplete");
        vm.setHost(new Host());
        try {
            CompletenessAssertor.validateParameters(vm, "host.id");
            fail("expected WebApplicationException on incomplete model");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "VM", "host.id");
        }
    }

    @Test
    public void testMissingSubFieldAlternatives() throws Exception {
        VM vm = new VM();
        vm.setDescription("incomplete");
        vm.setHost(new Host());
        try {
            CompletenessAssertor.validateParameters(vm, "host.id|name");
            fail("expected WebApplicationException on incomplete model");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "VM", "host.id|name");
        }
    }

    @Test
    public void testMissingSuperFieldAlternatives() throws Exception {
        Permission permission = new Permission();
        permission.setVm(new VM());
        try {
            CompletenessAssertor.validateParameters(permission, "user|vm.name");
            fail("expected WebApplicationException on incomplete model");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "Permission", "user|vm.name");
        }
    }

    @Test
    public void testMissingBothAlternatives() throws Exception {
        Permission permission = new Permission();
        permission.setVm(new VM());
        try {
            CompletenessAssertor.validateParameters(permission, "user|vm.name|id");
            fail("expected WebApplicationException on incomplete model");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "Permission", "user|vm.name|id");
        }
    }

    @Test
    public void testCompleteSubField() throws Exception {
        VM vm = new VM();
        vm.setHost(new Host());
        vm.getHost().setId("0");
        CompletenessAssertor.validateParameters(vm, "host.id");
    }

    @Test
    public void testCompleteSubFieldAlternatives() throws Exception {
        VM vm = new VM();
        vm.setHost(new Host());
        vm.getHost().setName("zog");
        CompletenessAssertor.validateParameters(vm, "host.id|name");
    }

    @Test
    public void testCompleteSuperFieldAlternatives() throws Exception {
        Permission permission = new Permission();
        permission.setUser(new User());
        permission.getUser().setName("joe");
        CompletenessAssertor.validateParameters(permission, "vm|user.name");
    }

    @Test
    public void testCompleteBothAlternatives() throws Exception {
        Permission permission = new Permission();
        permission.setUser(new User());
        permission.getUser().setName("joe");
        CompletenessAssertor.validateParameters(permission, "vm|user.name|id");
    }

    @Test
    public void testCompleteListSubField() throws Exception {
        User user = new User();
        user.setRoles(new Roles());
        user.getRoles().getRoles().add(new Role());
        user.getRoles().getRoles().get(0).setId("0");
        user.getRoles().getRoles().add(new Role());
        user.getRoles().getRoles().get(1).setId("0");
        CompletenessAssertor.validateParameters(user, "roles.id");
    }

    @Test
    public void testCompleteListSubFieldAlternatives() throws Exception {
        User user = new User();
        user.setRoles(new Roles());
        user.getRoles().getRoles().add(new Role());
        user.getRoles().getRoles().get(0).setId("0");
        user.getRoles().getRoles().add(new Role());
        user.getRoles().getRoles().get(1).setName("0");
        CompletenessAssertor.validateParameters(user, "roles.id|name");
    }

    @Test
    public void testMissingListSubField() throws Exception {
        User user = new User();
        user.setRoles(new Roles());
        user.getRoles().getRoles().add(new Role());
        user.getRoles().getRoles().get(0).setId("0");
        user.getRoles().getRoles().add(new Role());
        user.getRoles().getRoles().get(1).setName("0");
        try {
            CompletenessAssertor.validateParameters(user, "roles.id");
            fail("expected WebApplicationException on incomplete model");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "User", "roles.id");
        }
    }

    @Test
    public void testMissingListSubFieldAlternatives() throws Exception {
        User user = new User();
        user.setRoles(new Roles());
        user.getRoles().getRoles().add(new Role());
        user.getRoles().getRoles().get(0).setId("0");
        user.getRoles().getRoles().add(new Role());
        user.getRoles().getRoles().get(1).setDescription("0");
        try {
            CompletenessAssertor.validateParameters(user, "roles.id|name");
            fail("expected WebApplicationException on incomplete model");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "User", "roles.id|name");
        }
    }

    private void verifyIncompleteException(WebApplicationException wae, String type,  String... fields) {
        assertEquals(400, wae.getResponse().getStatus());
        Fault fault = (Fault)wae.getResponse().getEntity();
        assertNotNull(fault);
        assertEquals("Incomplete parameters", fault.getReason());
        String method = new Throwable().getStackTrace()[1].getMethodName();
        assertEquals(type + " " +  Arrays.asList(fields) + " required for " + method, fault.getDetail());
    }
}

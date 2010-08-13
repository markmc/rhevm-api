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
    public void testCompleteParameters() throws Exception {
        VM vm = new VM();
        vm.setName("foo");
        vm.setStatus(VmStatus.RUNNING);
        vm.setHost(new Host());
        CompletenessAssertor.validateParameters(vm, "name", "host", "status");
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

    private void verifyIncompleteException(WebApplicationException wae, String type,  String... fields) {
        assertEquals(400, wae.getResponse().getStatus());
        Fault fault = (Fault)wae.getResponse().getEntity();
        assertNotNull(fault);
        assertEquals("Incomplete parameters", fault.getReason());
        String method = new Throwable().getStackTrace()[1].getMethodName();
        assertEquals(type + " " +  Arrays.asList(fields) + " required for " + method, fault.getDetail());
    }
}
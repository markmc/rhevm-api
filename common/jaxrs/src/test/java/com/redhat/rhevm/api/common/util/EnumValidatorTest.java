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
package com.redhat.rhevm.api.common.util;

import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;

import com.redhat.rhevm.api.model.Fault;

import static com.redhat.rhevm.api.common.util.EnumValidator.validateEnum;

public class EnumValidatorTest extends Assert {

    @Test
    public void testValid() throws Exception {
        assertEquals(Thread.State.NEW, validateEnum(Thread.State.class, "NEW"));
    }

    @Test
    public void testInvalid() throws Exception {
        try {
            validateEnum(Thread.State.class, "foobar");
            fail("expected WebApplicationException on invalid value");
        } catch (WebApplicationException wae) {
            verifyInvalidValueException(wae, "foobar", "State");
        }
    }

    private void verifyInvalidValueException(WebApplicationException wae, String value, String typeName) {
        assertEquals(400, wae.getResponse().getStatus());
        Fault fault = (Fault)wae.getResponse().getEntity();
        assertNotNull(fault);
        assertEquals("Invalid value", fault.getReason());
        assertEquals(value + " is not a member of " + typeName, fault.getDetail());
    }
}

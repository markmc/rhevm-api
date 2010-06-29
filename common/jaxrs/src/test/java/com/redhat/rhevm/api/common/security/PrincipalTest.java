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
package com.redhat.rhevm.api.common.security;

import org.junit.Assert;
import org.junit.Test;

public class PrincipalTest extends Assert {

    private void assertNotEquals(Principal a, Principal b) {
        assertFalse(a.equals(b));
    }

    @Test
    public void testEquals() {
        Principal a = new Principal("zog", "zig", "zogabongs");
        Principal b = new Principal("zog", "zig", "zogabongs");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testEqualsNone() {
        Principal a = new Principal(null, null, null);
        Principal b = Principal.NONE;
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testNotEqualsNull() {
        Principal a = new Principal("zog", "zig", "zogabongs");
        assertNotEquals(a, null);
    }

    @Test
    public void testNotEqualsNullDomain() {
        Principal a = new Principal("zog", "zig", "zogabongs");
        Principal b = new Principal(null, "zig", "zogabongs");
        assertNotEquals(a, b);
    }

    @Test
    public void testNotEqualsNullUser() {
        Principal a = new Principal("zog", "zig", "zogabongs");
        Principal b = new Principal("zog", null, "zogabongs");
        assertNotEquals(a, b);
    }

    @Test
    public void testNotEqualsNullSecret() {
        Principal a = new Principal("zog", "zig", "zogabongs");
        Principal b = new Principal("zog", "zig", null);
        assertNotEquals(a, b);
    }

    @Test
    public void testNotEqualsRandomDomain() {
        Principal a = new Principal("zog", "zig", "zogabongs");
        Principal b = new Principal("dustintheturkey", "zig", "zogabongs");
        assertNotEquals(a, b);
    }

    @Test
    public void testNotEqualsRandomUser() {
        Principal a = new Principal("zog", "zig", "zogabongs");
        Principal b = new Principal("zog", "dustintheturkey", "zogabongs");
        assertNotEquals(a, b);
    }

    @Test
    public void testNotEqualsRandomSecret() {
        Principal a = new Principal("zog", "zig", "zogabongs");
        Principal b = new Principal("zog", "zig", "dustintheturkey");
        assertNotEquals(a, b);
    }
}

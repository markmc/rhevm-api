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

import org.junit.Assert;
import org.junit.Test;

import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.Host;

public class QueryTransformerTest extends Assert {

    private static final QueryTransformer transformer = new QueryTransformer();

    private static String htrans(String query) {
        return transformer.transform(Host.class, query);
    }

    @Test
    public void testHostNoTransform() throws Exception {
        assertEquals("Hosts:", htrans("Hosts:"));
    }

    @Test
    public void testHostBasicUpper() throws Exception {
        assertEquals("Hosts: STATUS = up", htrans("Hosts: STATUS = ACTIVE"));
    }

    @Test
    public void testHostBasicLower() throws Exception {
        assertEquals("Hosts: status = up", htrans("Hosts: status = active"));
    }

    @Test
    public void testHostBasicMixed() throws Exception {
        assertEquals("Hosts: stATus = up", htrans("Hosts: stATus = acTIve"));
    }

    @Test
    public void testHostInActive() throws Exception {
        assertEquals("Hosts: status = maintenance", htrans("Hosts: status = inactive"));
    }

    @Test
    public void testHostPendingApproval() throws Exception {
        assertEquals("Hosts: status = pendingapproval", htrans("Hosts: status = PENDING_APPROVAL"));
    }

    @Test
    public void testHostDown() throws Exception {
        assertEquals("Hosts: status = down", htrans("Hosts: status = down"));
    }

    @Test
    public void testHostError() throws Exception {
        assertEquals("Hosts: status = ERROR", htrans("Hosts: status = ERROR"));
    }

    @Test
    public void testHostNoSpace() throws Exception {
        assertEquals("Hosts:status=up", htrans("Hosts:status=ACTIVE"));
    }

    @Test
    public void testHostNotEquals() throws Exception {
        assertEquals("Hosts: status != up", htrans("Hosts: status != ACTIVE"));
    }

    @Test
    public void testHostLong() throws Exception {
        assertEquals("Hosts: status != up sortby name asc page 1", htrans("Hosts: status != ACTIVE sortby name asc page 1"));
    }

    @Test
    public void testHostMessedUp() throws Exception {
        assertEquals("Hosts    :NAME != foo        AND StatuS       =     up", htrans("Hosts    :NAME != foo        AND StatuS       =     AcTiVe"));
    }

    @Test
    public void testClusterNoTransform() throws Exception {
        assertEquals("Clusters: foo=bar", transformer.transform(Cluster.class, "Clusters: foo=bar"));
    }
}

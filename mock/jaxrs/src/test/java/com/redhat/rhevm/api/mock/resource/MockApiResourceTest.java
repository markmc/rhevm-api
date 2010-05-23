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
package com.redhat.rhevm.api.mock.resource;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.jboss.resteasy.client.ClientResponse;
import com.redhat.rhevm.api.model.LinkHeader;
import com.redhat.rhevm.api.model.Link;

public class MockApiResourceTest extends MockTestBase {
    @Test
    public void testEntryPoint() throws Exception {
        ClientResponse<Object> response = api.head();

        assertEquals(Response.Status.Family.SUCCESSFUL, response.getResponseStatus().getFamily());
        assertEquals(Response.Status.OK, response.getResponseStatus());

        MultivaluedMap<String, String> headers = response.getHeaders();

        List<String> linkHeaders = headers.get("Link");

        assertNotNull(linkHeaders);

        List<Link> links = new ArrayList<Link>();

        for (String s : linkHeaders) {
            for (String t : s.split(",")) {
                Link l = LinkHeader.parse(t);
                assertEquals(t, LinkHeader.format(l));
                links.add(l);
            }
        }

        int i = 12;
        assertEquals(i, links.size());
        assertEquals("vms/search",            links.get(--i).getRel());
        assertEquals("vms",                   links.get(--i).getRel());
        assertEquals("storagedomains/search", links.get(--i).getRel());
        assertEquals("storagedomains",        links.get(--i).getRel());
        assertEquals("hosts/search",          links.get(--i).getRel());
        assertEquals("hosts",                 links.get(--i).getRel());
        assertEquals("datacenters/search",    links.get(--i).getRel());
        assertEquals("datacenters",           links.get(--i).getRel());
        assertEquals("cpus/search",           links.get(--i).getRel());
        assertEquals("cpus",                  links.get(--i).getRel());
        assertEquals("clusters/search",       links.get(--i).getRel());
        assertEquals("clusters",              links.get(--i).getRel());
        assertEquals(0, i);
    }
}

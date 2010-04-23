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
package com.redhat.rhevm.api.dummy.resource;

import java.util.HashMap;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.model.Hosts;
import com.redhat.rhevm.api.resource.HostResource;
import com.redhat.rhevm.api.resource.HostsResource;
import com.redhat.rhevm.api.dummy.model.DummyHost;

public class DummyHostsResource implements HostsResource {
    /* FIXME: would like to do:
     * private @Context UriInfo uriInfo;
     */

    /* FIXME: synchronize access to this */
    private static HashMap<String, DummyHostResource> hosts = new HashMap<String, DummyHostResource>();

    static {
        while (hosts.size() < 4) {
            DummyHostResource host = new DummyHostResource(new DummyHost());
            host.getHost().setName("host" + Integer.toString(hosts.size()));
            hosts.put(host.getHost().getId(), host);
        }
    }

    @Override
    public Hosts list(UriInfo uriInfo) {
        Hosts ret = new Hosts();

        for (DummyHostResource host : hosts.values()) {
            String id = host.getHost().getId();
            UriBuilder uriBuilder = uriInfo.getRequestUriBuilder().path(id);
            ret.getHosts().add(host.addLinks(uriBuilder));
        }

        return ret;
    }

    @Override
    public Response add(UriInfo uriInfo, Host host) {
        DummyHostResource newHost = new DummyHostResource(new DummyHost(host));

        String id = newHost.getHost().getId();
        hosts.put(id, newHost);

        UriBuilder uriBuilder = uriInfo.getRequestUriBuilder().path(id);

        host = newHost.addLinks(uriBuilder);

        return Response.created(uriBuilder.build()).entity(host).build();
    }

    @Override
    public void remove(String id) {
        hosts.remove(id);
    }

    @Override
    public HostResource getHostSubResource(UriInfo uriInfo, String id) {
        return hosts.get(id);
    }
}

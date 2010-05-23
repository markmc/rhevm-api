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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.api.mock.util.SimpleQueryEvaluator;
import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.model.Hosts;
import com.redhat.rhevm.api.resource.HostResource;
import com.redhat.rhevm.api.resource.HostsResource;

import static com.redhat.rhevm.api.mock.resource.AbstractMockResource.allocateId;


public class MockHostsResource extends AbstractMockQueryableResource<Host> implements HostsResource {
    /* FIXME: would like to do:
     * private @Context UriInfo uriInfo;
     */

    private static Map<String, MockHostResource> hosts =
        Collections.synchronizedMap(new HashMap<String, MockHostResource>());

    public MockHostsResource() {
        super(new SimpleQueryEvaluator<Host>());
    }

    public void populate() {
        synchronized (hosts) {
            while (hosts.size() < 4) {
                MockHostResource resource = new MockHostResource(allocateId(Host.class), getExecutor());
                resource.getModel().setName("host" + resource.getModel().getId());
                hosts.put(resource.getModel().getId(), resource);
            }
        }
    }

    @Override
    public Hosts list(UriInfo uriInfo) {
        Hosts ret = new Hosts();

        for (MockHostResource host : hosts.values()) {
            if (filter(host.getModel(), uriInfo, Host.class)) {
                String id = host.getModel().getId();
                UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(id);
                ret.getHosts().add(host.addLinks(uriBuilder));
            }
        }

        return ret;
    }

    @Override
    public Response add(UriInfo uriInfo, Host host) {
        MockHostResource resource = new MockHostResource(allocateId(Host.class), getExecutor());

        resource.updateModel(host);

        String id = resource.getId();
        hosts.put(id, resource);

        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(id);

        host = resource.addLinks(uriBuilder);

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

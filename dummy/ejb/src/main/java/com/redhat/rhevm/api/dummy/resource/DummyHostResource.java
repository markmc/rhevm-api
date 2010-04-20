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

import javax.ejb.Stateless;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Actions;
import com.redhat.rhevm.api.model.Link;
import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.model.Hosts;
import com.redhat.rhevm.api.resource.HostResource;
import com.redhat.rhevm.api.dummy.model.DummyHost;

@Stateless
public class DummyHostResource implements HostResource
{
	/* FIXME: would like to do:
	 * private @Context UriInfo uriInfo;
	 */

	/* FIXME: synchronize access to this */
	private static HashMap<String, DummyHost> hosts = new HashMap<String, DummyHost>();

	static {
		while (hosts.size() < 4) {
			DummyHost host = new DummyHost();
			host.setName("host" + Integer.toString(hosts.size()));
			hosts.put(host.getId(), host);
		}
	}

	private UriBuilder getUriBuilder(UriInfo uriInfo, String id) {
		return uriInfo.getBaseUriBuilder().clone().path("hosts").path(id);
	}

	private Host addLinks(Host host, UriBuilder uriBuilder) {
		host.setLink(new Link("self", uriBuilder.build()));
		host.setActions(new Actions(uriBuilder, HostResource.class));
		return new Host(host);
	}

	private Host addLinks(Host host, UriInfo uriInfo) {
		return addLinks(host, getUriBuilder(uriInfo, host.getId()));
	}

	/* FIXME: kill uriInfo param, make href auto-generated? */
	@Override
	public Host get(UriInfo uriInfo, String id) {
		DummyHost host = hosts.get(id);

		return addLinks(host, uriInfo);
	}

	@Override
	public Hosts list(UriInfo uriInfo) {
		Hosts ret = new Hosts();

		for (DummyHost host : hosts.values()) {
			ret.getHosts().add(addLinks(host, uriInfo));
		}

		return ret;
	}

	@Override
	public Response add(UriInfo uriInfo, Host host) {
		DummyHost newHost = new DummyHost(host);

		hosts.put(newHost.getId(), newHost);

		UriBuilder uriBuilder = getUriBuilder(uriInfo, newHost.getId());

		host = addLinks(newHost, uriBuilder);

		return Response.created(uriBuilder.build()).entity(host).build();
	}

	@Override
	public Host update(UriInfo uriInfo, String id, Host host) {
		DummyHost ret = hosts.get(id);
		ret.update(host);
		return addLinks(ret, uriInfo);
	}

	@Override
	public void remove(String id) {
		hosts.remove(id);
	}

	@Override
	public void approve(String id) {
	}

	@Override
	public void fence(String id) {
	}

	@Override
	public void resume(String id) {
	}

/*
	@Override
	public void connectStorage(String id, String storageDevice) {
	}
*/
}

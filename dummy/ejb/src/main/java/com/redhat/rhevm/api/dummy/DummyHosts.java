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
package com.redhat.rhevm.api.dummy;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import javax.ejb.Stateless;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.Actions;
import com.redhat.rhevm.api.Link;
import com.redhat.rhevm.api.Host;
import com.redhat.rhevm.api.Hosts;

@Stateless
public class DummyHosts implements Hosts
{
	/* FIXME: would like to do:
	 * private @Context UriInfo uriInfo;
	 */

	/* FIXME: synchronize access to this */
	private static HashMap<String, DummyHost> hosts = new HashMap<String, DummyHost>();

	static {
		while (hosts.size() < 4) {
			DummyHost host = new DummyHost();
			hosts.put(host.getId(), host);
		}
	}

	private UriBuilder getUriBuilder(UriInfo uriInfo, String id) {
		return uriInfo.getBaseUriBuilder().clone().path("hosts").path(id);
	}

	private DummyHost addLinks(DummyHost host, UriBuilder uriBuilder) {
		host.setLink(new Link("self", uriBuilder.build(), "application/xml"));
		host.setActions(new Actions(uriBuilder, Hosts.class));
		return host;
	}

	/* FIXME: kill uriInfo param, make href auto-generated? */
	private DummyHost lookup(UriInfo uriInfo, String id) {
		return addLinks(hosts.get(id), getUriBuilder(uriInfo, id));
	}

	@Override
	public Host get(UriInfo uriInfo, String id) {
		return lookup(uriInfo, id);
	}

	@Override
	public List<Host> list(UriInfo uriInfo) {
		List<Host> ret = new ArrayList<Host>();

		for (DummyHost host : hosts.values()) {
			/* FIXME: the extra lookup is just to add href */
			ret.add(lookup(uriInfo, host.getId()));
		}

		return ret;
	}

	@Override
	public Response add(UriInfo uriInfo, Host host) {
		DummyHost newHost = new DummyHost(host);

		hosts.put(newHost.getId(), newHost);

		UriBuilder uriBuilder = getUriBuilder(uriInfo, newHost.getId());

		addLinks(newHost, uriBuilder);

		return Response.created(uriBuilder.build()).entity(newHost).build();
	}

	@Override
	public Host update(UriInfo uriInfo, String id, Host host) {
		DummyHost ret = lookup(uriInfo, id);
		ret.update(host);
		return ret;
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

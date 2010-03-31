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
import javax.ws.rs.core.UriInfo;

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

	/* FIXME: kill uriInfo param, make href auto-generated? */
	public static DummyHost lookup(UriInfo uriInfo, String id) {
		DummyHost host = hosts.get(id);
		if (uriInfo != null)
			host.setHref(uriInfo.getBaseUriBuilder().clone().path("hosts").path(id).build());
		return host;
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
		/* FIXME: the extra lookup is just to add href */
		newHost = lookup(uriInfo, newHost.getId());

		return Response.created(newHost.getHref()).build();
	}

	@Override
	public Host update(String id, Host host) {
		DummyHost ret = hosts.get(id);
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

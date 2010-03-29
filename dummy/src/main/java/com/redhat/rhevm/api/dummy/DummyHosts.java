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
import javax.jws.WebService;

import com.redhat.rhevm.api.Host;
import com.redhat.rhevm.api.Hosts;

@Stateless
@WebService
public class DummyHosts implements Hosts
{
	private static HashMap<String, DummyHost> hosts = new HashMap<String, DummyHost>();

	static {
		while (hosts.size() < 4) {
			DummyHost host = new DummyHost();
			hosts.put(host.getId(), host);
		}
	}

	public static DummyHost lookup(String id) {
		return hosts.get(id);
	}

	@Override
	public Host get(String id) {
		return lookup(id);
	}

	@Override
	public List<Host> list() {
		List<Host> ret = new ArrayList<Host>();

		for (DummyHost host : hosts.values())
			ret.add(host);

		return ret;
	}

	@Override
	public List<Host> search(String criteria) {
		return new ArrayList<Host>();
	}

	@Override
	public Host add(Host host) {
		DummyHost ret = new DummyHost();
		ret.update(host);
		hosts.put(ret.getId(), ret);
		return ret;
	}

	@Override
	public Host update(Host host) {
		DummyHost ret = hosts.get(host.getId());
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

	@Override
	public void connectStorage(String id, String storageDevice) {
	}
}

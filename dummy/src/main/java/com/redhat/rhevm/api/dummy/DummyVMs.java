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

import com.redhat.rhevm.api.VM;
import com.redhat.rhevm.api.VMs;

@Stateless
@WebService
public class DummyVMs implements VMs
{
	private static HashMap<String, DummyVM> vms = new HashMap<String, DummyVM>();

	static {
		while (vms.size() < 10) {
			DummyVM vm = new DummyVM();
			vms.put(vm.getId(), vm);
		}
	}

	@Override
	public VM get(String id) {
		return vms.get(id);
	}

	@Override
	public List<VM> list() {
		List<VM> ret = new ArrayList<VM>();

		for (DummyVM vm : vms.values())
			ret.add(vm);

		return ret;
	}

	@Override
	public List<VM> search(String criteria) {
		return new ArrayList<VM>();
	}

	@Override
	public VM add(VM vm) {
		DummyVM ret = new DummyVM();
		ret.update(vm);
		vms.put(ret.getId(), ret);
		return ret;
	}

	@Override
	public VM update(VM vm) {
		DummyVM ret = vms.get(vm.getId());
		ret.update(vm);
		return ret;
	}

	@Override
	public void remove(String id) {
		vms.remove(id);
	}

	@Override
	public void run(String id) {
		vms.get(id).setStatus(DummyVMStatus.UP);
	}

	@Override
	public void stop(String id) {
		vms.get(id).setStatus(DummyVMStatus.DOWN);
	}

	@Override
	public void pause(String id) {
		vms.get(id).setStatus(DummyVMStatus.PAUSED);
	}

	@Override
	public void shutdown(String id) {
		vms.get(id).setStatus(DummyVMStatus.DOWN);
	}

	@Override
	public void restore(String id) {
	}

	@Override
	public void migrate(String id) {
	}

	@Override
	public void move(String id) {
	}

	@Override
	public void detach(String id) {
	}

	@Override
	public void changeCD(String id) {
	}

	@Override
	public void ejectCD(String id) {
	}
}

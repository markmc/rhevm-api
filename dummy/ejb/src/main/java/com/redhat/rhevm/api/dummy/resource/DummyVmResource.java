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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ejb.Stateless;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Actions;
import com.redhat.rhevm.api.model.Link;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.resource.VmResource;
import com.redhat.rhevm.api.dummy.model.DummyVmStatus;
import com.redhat.rhevm.api.dummy.model.DummyVM;

@Stateless
public class DummyVmResource implements VmResource
{
	/* FIXME: would like to do:
	 * private @Context UriInfo uriInfo;
	 */

	/* FIXME: synchronize access to this */
	private static HashMap<String, DummyVM> vms = new HashMap<String, DummyVM>();

	static {
		while (vms.size() < 10) {
			DummyVM vm = new DummyVM();
			vms.put(vm.getId(), vm);
		}
	}

	private UriBuilder getUriBuilder(UriInfo uriInfo, String id) {
		return uriInfo.getBaseUriBuilder().clone().path("vms").path(id);
	}

	private VM addLinks(VM vm, UriBuilder uriBuilder) {
		vm.setLink(new Link("self", uriBuilder.build()));
		vm.setActions(new Actions(uriBuilder, VmResource.class));
		return new VM(vm);
	}

	private VM addLinks(VM vm, UriInfo uriInfo) {
		return addLinks(vm, getUriBuilder(uriInfo, vm.getId()));
	}

	/* FIXME: kill uriInfo param, make href auto-generated? */
	@Override
	public VM get(UriInfo uriInfo, String id) {
		DummyVM vm = vms.get(id);

		return addLinks(vm, uriInfo);
	}

	@Override
	public List<VM> list(UriInfo uriInfo) {
		List<VM> ret = new ArrayList<VM>();

		for (DummyVM vm : vms.values()) {
			ret.add(addLinks(vm, uriInfo));
		}

		return ret;
	}

	@Override
	public Response add(UriInfo uriInfo, VM vm) {
		DummyVM newVM = new DummyVM(vm);

		vms.put(newVM.getId(), newVM);

		UriBuilder uriBuilder = getUriBuilder(uriInfo, newVM.getId());

		vm = addLinks(newVM, uriBuilder);

		return Response.created(uriBuilder.build()).entity(vm).build();
	}

	@Override
	public VM update(UriInfo uriInfo, String id, VM vm) {
		DummyVM ret = vms.get(id);
		ret.update(vm);
		return addLinks(ret, uriInfo);
	}

	@Override
	public void remove(String id) {
		vms.remove(id);
	}

	@Override
	public void start(String id) {
		vms.get(id).setStatus(DummyVmStatus.UP);
	}

	@Override
	public void stop(String id) {
		vms.get(id).setStatus(DummyVmStatus.DOWN);
	}

	@Override
	public void shutdown(String id) {
		vms.get(id).setStatus(DummyVmStatus.DOWN);
	}

	@Override
	public void suspend(String id) {
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

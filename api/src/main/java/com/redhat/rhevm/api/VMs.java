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
package com.redhat.rhevm.api;

import javax.jws.WebMethod;
import java.util.List;

public interface VMs
{
	@WebMethod public VM get(String id);

	@WebMethod public List<VM> list();
	@WebMethod public List<VM> search(String criteria);

	/**
	 * Creates a new VM and adds it to the database. The VM is created
	 * based on the properties of @vm.
	 * <p>
	 * The VM#name, VM#templateId and VM#clusterId properties are required.
	 *
	 * @param vm  the VM definition from which to create the new VM
	 * @return    the new newly created VM
	 */
	@WebMethod public VM add(VM vm);

	/**
	 * Modifies an existing VM's properties in the database.
	 * <p>
	 * Only the VM#name property can be modified.
	 *
	 * @param vm  the VM definition with the modified properties
	 * @return    the updated VM
	 */
	@WebMethod public VM update(VM vm);

	@WebMethod public void remove(String id);

	@WebMethod public void start(String id);
	@WebMethod public void stop(String id);
	@WebMethod public void shutdown(String id);

	@WebMethod public void suspend(String id);
	@WebMethod public void restore(String id);
	@WebMethod public void migrate(String id);

	@WebMethod public void move(String id);
	@WebMethod public void detach(String id);

	@WebMethod public void changeCD(String id);
	@WebMethod public void ejectCD(String id);
}

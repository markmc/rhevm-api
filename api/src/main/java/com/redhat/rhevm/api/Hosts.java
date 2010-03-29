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

public interface Hosts
{
	@WebMethod public Host get(String id);

	@WebMethod public List<Host> list();
	@WebMethod public List<Host> search(String criteria);

	/**
	 * Creates a new host and adds it to the database. The host is
	 * created based on the properties of @host.
	 * <p>
	 * The Host#name, Host#address and Host#rootPassword properties
	 * are required.
	 *
	 * @param host  the host definition from which to create the new
	 *              host
	 * @return      the new newly created Host
	 */
	@WebMethod public Host add(Host host);

	/**
	 * Modifies an existing host's properties in the database.
	 * <p>
	 * Only the Host#name property can be modified.
	 *
	 * @param host  the host definition with the modified properties
	 * @return      the updated Host
	 */
	@WebMethod public Host update(Host host);

	@WebMethod public void remove(String id);

	@WebMethod public void approve(String id);
	@WebMethod public void fence(String id);
	@WebMethod public void resume(String id);

	@WebMethod public void connectStorage(String id, String storageDevice);
}

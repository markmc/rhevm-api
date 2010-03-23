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

	@WebMethod public VM add(VM vm);
	@WebMethod public VM update(VM vm);

	@WebMethod public void remove(String id);

	@WebMethod public void run(String id);
	@WebMethod public void stop(String id);
	@WebMethod public void pause(String id);
	@WebMethod public void shutdown(String id);

	@WebMethod public void restore(String id);
	@WebMethod public void migrate(String id);

	@WebMethod public void move(String id);
	@WebMethod public void detach(String id);

	@WebMethod public void changeCD(String id);
	@WebMethod public void ejectCD(String id);
}

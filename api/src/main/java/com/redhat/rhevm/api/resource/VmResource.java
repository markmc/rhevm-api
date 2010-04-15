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
package com.redhat.rhevm.api.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/* FIXME: doesn't seem to do anything ? Also, we could do without
 *        the explicit dependency on RESTeasy
 */
import org.jboss.resteasy.annotations.providers.jaxb.Formatted;

import java.util.List;

import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.VM;

/* FIXME: we want to produce JSON too */

@Path("/vms")
@Produces({MediaType.RHEVM_VM_XML, MediaType.RHEVM_VM_YAML, MediaType.RHEVM_VM_JSON})
@Formatted
public interface VmResource
{
	/* FIXME: can we make uriInfo a field instead of a parameter to
	 *        each method? Adding @Context to the implementation
	 *        class doesn't seem to work.
	 */

	@GET
	@Path("{id}")
	public VM get(@Context UriInfo uriInfo, @PathParam("id") String id);

	/* FIXME: do we want to define our own collection type for
	 *        the return value here rather than <collection> ?
	 */
	@GET
	public List<VM> list(@Context UriInfo uriInfo);

	/* FIXME: need to move this to e.g. a top-level /search
	 * @GET
	 * public List<VM> search(String criteria);
	 */

	/**
	 * Creates a new VM and adds it to the database. The VM is created
	 * based on the properties of @vm.
	 * <p>
	 * The VM#name, VM#templateId and VM#clusterId properties are required.
	 *
	 * @param vm  the VM definition from which to create the new VM
	 * @return    the new newly created VM
	 */
	@POST
	@Consumes({MediaType.RHEVM_VM_XML, MediaType.RHEVM_VM_YAML, MediaType.RHEVM_VM_JSON})
	@Produces({MediaType.RHEVM_VM_XML, MediaType.RHEVM_VM_YAML, MediaType.RHEVM_VM_JSON})
	public Response add(@Context UriInfo uriInfo, VM vm);

	/**
	 * Modifies an existing VM's properties in the database.
	 * <p>
	 * Only the VM#name property can be modified.
	 *
	 * @param vm  the VM definition with the modified properties
	 * @return    the updated VM
	 */
	@PUT
	@Path("{id}")
	@Consumes({MediaType.RHEVM_VM_XML, MediaType.RHEVM_VM_YAML, MediaType.RHEVM_VM_JSON})
	public VM update(@Context UriInfo uriInfo, @PathParam("id") String id, VM vm);

	@DELETE
	@Path("{id}")
	public void remove(@PathParam("id") String id);

	/* FIXME:
	 * we need to list the available action URLs in the VM entity
	 */

	@POST
	@Action
	@Path("{id}/start")
	public void start(@PathParam("id") String id);

	@POST
	@Action
	@Path("{id}/stop")
	public void stop(@PathParam("id") String id);

	@POST
	@Action
	@Path("{id}/shutdown")
	public void shutdown(@PathParam("id") String id);

	@POST
	@Action
	@Path("{id}/suspend")
	public void suspend(@PathParam("id") String id);

	@POST
	@Action
	@Path("{id}/restore")
	public void restore(@PathParam("id") String id);

	@POST
	@Action
	@Path("{id}/migrate")
	public void migrate(@PathParam("id") String id);

	@POST
	@Action
	@Path("{id}/move")
	public void move(@PathParam("id") String id);

	@POST
	@Action
	@Path("{id}/detach")
	public void detach(@PathParam("id") String id);

	@POST
	@Action
	@Path("{id}/changeCD")
	public void changeCD(@PathParam("id") String id);

	@POST
	@Action
	@Path("{id}/ejectCD")
	public void ejectCD(@PathParam("id") String id);
}

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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response;

import com.redhat.rhevm.api.model.Actionable;
import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.VM;

public interface VmResource {
    /* FIXME: can we make uriInfo a field instead of a parameter to
     *        each method? Adding @Context to the implementation
     *        class doesn't seem to work.
     */

    @GET
    public VM get(@Context UriInfo uriInfo);

    /**
     * Modifies an existing VM's properties in the database.
     * <p>
     * Only the VM#name property can be modified.
     *
     * @param vm  the VM definition with the modified properties
     * @return    the updated VM
     */
    @PUT
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_X_YAML, MediaType.APPLICATION_JSON})
    public VM update(@Context UriInfo uriInfo, VM vm);
    
    @Path("{action: (start|stop|shutdown|suspend|restore|migrate|move|detach|changeCD|ejectCD)}/{oid}")
    public ActionResource getActionSubresource(@PathParam("action")String action, @PathParam("oid")String oid);

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_X_YAML, MediaType.APPLICATION_JSON})
    @Actionable
    @Path("start")
    public Response start(@Context UriInfo uriInfo, Action action);
 
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_X_YAML, MediaType.APPLICATION_JSON})
    @Actionable
    @Path("stop")
    public Response stop(@Context UriInfo uriInfo, Action action);
    
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_X_YAML, MediaType.APPLICATION_JSON})
    @Actionable
    @Path("shutdown")
    public Response shutdown(@Context UriInfo uriInfo, Action action);

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_X_YAML, MediaType.APPLICATION_JSON})
    @Actionable
    @Path("suspend")
    public Response suspend(@Context UriInfo uriInfo, Action action);

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_X_YAML, MediaType.APPLICATION_JSON})
    @Actionable
    @Path("restore")
    public Response restore(@Context UriInfo uriInfo, Action action);

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_X_YAML, MediaType.APPLICATION_JSON})
    @Actionable
    @Path("migrate")
    public Response migrate(@Context UriInfo uriInfo, Action action);

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_X_YAML, MediaType.APPLICATION_JSON})
    @Actionable
    @Path("move")
    public Response move(@Context UriInfo uriInfo, Action action);

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_X_YAML, MediaType.APPLICATION_JSON})
    @Actionable
    @Path("detach")
    public Response detach(@Context UriInfo uriInfo, Action action);

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_X_YAML, MediaType.APPLICATION_JSON})
    @Actionable
    @Path("changeCD")
    public Response changeCD(@Context UriInfo uriInfo, Action action);

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_X_YAML, MediaType.APPLICATION_JSON})
    @Actionable
    @Path("ejectCD")
    public Response ejectCD(@Context UriInfo uriInfo, Action action);
}

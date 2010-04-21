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
import javax.ws.rs.Produces;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

/* FIXME: doesn't seem to do anything ? Also, we could do without
 *        the explicit dependency on RESTeasy
 */
import org.jboss.resteasy.annotations.providers.jaxb.Formatted;

import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.VM;

@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_X_YAML, MediaType.APPLICATION_JSON})
@Formatted
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

    @POST
    @Action
    @Path("start")
    public void start();

    @POST
    @Action
    @Path("stop")
    public void stop();

    @POST
    @Action
    @Path("shutdown")
    public void shutdown();

    @POST
    @Action
    @Path("suspend")
    public void suspend();

    @POST
    @Action
    @Path("restore")
    public void restore();

    @POST
    @Action
    @Path("migrate")
    public void migrate();

    @POST
    @Action
    @Path("move")
    public void move();

    @POST
    @Action
    @Path("detach")
    public void detach();

    @POST
    @Action
    @Path("changeCD")
    public void changeCD();

    @POST
    @Action
    @Path("ejectCD")
    public void ejectCD();
}

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
import javax.ws.rs.core.Response;
import org.jboss.resteasy.annotations.providers.jaxb.Formatted;

import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.model.Hosts;


@Path("/hosts")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_X_YAML})
public interface HostsResource {

    @GET
    @Formatted
    public Hosts list();

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
    @POST
    @Formatted
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_X_YAML})
    public Response add(Host host);

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") String id);

    /**
     * Sub-resource locator method, returns individual HostResource on which the
     * remainder of the URI is dispatched.
     *
     * @param id  the Host ID
     * @return    matching subresource if found
     */
    @Path("{id}")
    public HostResource getHostSubResource(@PathParam("id") String id);
}

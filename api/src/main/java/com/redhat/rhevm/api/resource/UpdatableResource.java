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
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import org.jboss.resteasy.annotations.providers.jaxb.Formatted;

import com.redhat.rhevm.api.model.BaseResource;


@Produces(MediaType.APPLICATION_XML)
public interface UpdatableResource<R extends BaseResource> {
    /* FIXME: can we make uriInfo a field instead of a parameter to
     *        each method? Adding @Context to the implementation
     *        class doesn't seem to work.
     */

    @GET
    @Formatted
    public R get(@Context UriInfo uriInfo);

    @PUT
    @Formatted
    @Consumes(MediaType.APPLICATION_XML)
    public R update(@Context UriInfo uriInfo, R resource);
}

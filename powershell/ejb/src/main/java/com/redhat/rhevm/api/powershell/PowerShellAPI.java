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
package com.redhat.rhevm.api.powershell;

import java.net.URI;

import javax.ejb.Stateless;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.API;
import com.redhat.rhevm.api.Link;
import com.redhat.rhevm.api.MediaType;

/* FIXME: it'd be nice to move this whole thing into the
 *        top-level api package
 */

@Stateless
public class PowerShellAPI implements API
{
	@Override
	public Response head(UriInfo uriInfo)
	{
		UriBuilder absolute = uriInfo.getBaseUriBuilder();

		URI hostsUrl = absolute.clone().path("hosts").build();
		URI vmsUrl   = absolute.clone().path("vms").build();

		Response.ResponseBuilder builder = Response.ok();

		/* FIXME: consider whether we really want to use the Link: HTTP header */
		builder.header("Link", new Link("hosts", hostsUrl, MediaType.APPLICATION_XML));
		builder.header("Link", new Link("vms",   vmsUrl,   MediaType.APPLICATION_XML));

		return builder.build();
	}
}

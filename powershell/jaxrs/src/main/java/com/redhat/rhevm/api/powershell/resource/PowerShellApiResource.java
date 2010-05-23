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
package com.redhat.rhevm.api.powershell.resource;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.LinkHeader;
import com.redhat.rhevm.api.model.Link;
import com.redhat.rhevm.api.resource.ApiResource;

/* FIXME: it'd be nice to move this whole thing into the
 *        top-level api package
 */

public class PowerShellApiResource implements ApiResource {

    private static final String SEARCH_RELATION = "/search";
    private static final String SEARCH_TEMPLATE = "?search={query}";

    private void addHeader(Response.ResponseBuilder responseBuilder, UriBuilder uriBuilder, String ... path) {
        // concantenate links in a single header with a comma-separated value,
        // which is the canonical form according to:
        // http://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2
        //
        StringBuffer header = new StringBuffer();

        for (String p : path) {
            header.append(addPath(uriBuilder, p, p, "")).append(",");
            header.append(addPath(uriBuilder, p + SEARCH_RELATION, p, SEARCH_TEMPLATE)).append(",");
        }

        header.setLength(header.length() - 1);

        responseBuilder.header("Link", header);
    }

    @Override
    public Response head(UriInfo uriInfo) {
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();

        Response.ResponseBuilder responseBuilder = Response.ok();

        addHeader(responseBuilder, uriBuilder, "clusters", "cpus", "datacenters", "hosts", "storagedomains", "vms");

        return responseBuilder.build();
    }

    private String addPath(UriBuilder uriBuilder, String rel, String path, String query) {
        Link link = new Link();
        link.setRel(rel);
        link.setHref(uriBuilder.clone().path(path).build().toString() + query);
        return LinkHeader.format(link);
    }
}

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
package com.redhat.rhevm.api.common.resource;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.API;
import com.redhat.rhevm.api.model.LinkHeader;
import com.redhat.rhevm.api.model.Link;
import com.redhat.rhevm.api.model.ObjectFactory;
import com.redhat.rhevm.api.resource.ApiResource;
import com.redhat.rhevm.api.common.util.JAXBHelper;

public class DefaultApiResource implements ApiResource {

    private static final String SEARCH_RELATION = "/search";
    private static final String SEARCH_TEMPLATE = "?search={query}";

    protected final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    private static API api = new API();

    static {
        addLink("capabilities", false);
        addLink("clusters");
        addLink("datacenters");
        addLink("hosts");
        addLink("networks", false); // powershell has no select-network command
        addLink("roles", false);
        addLink("storagedomains");
        addLink("tags", false);
        addLink("templates");
        addLink("users");
        addLink("vmpools");
        addLink("vms");
    }

    private static void addLink(String rel, boolean searchable) {
        Link link = new Link();
        link.setRel(rel);
        link.setHref(rel + "/");
        api.getLinks().add(link);

        if (searchable) {
            link = new Link();
            link.setRel(rel + SEARCH_RELATION);
            link.setHref(rel + "/" + SEARCH_TEMPLATE);
            api.getLinks().add(link);
        }
    }

    private static void addLink(String rel) {
        addLink(rel, true);
    }

    private String addPath(UriBuilder uriBuilder, Link link) {
        String query = "";
        String path = link.getHref();

        // otherwise UriBuilder.build() will substitute {query}
        if (path.contains("?")) {
            query = path.substring(path.indexOf("?"));
            path = path.substring(0, path.indexOf("?") - 1);
        }

        link = JAXBHelper.clone(OBJECT_FACTORY.createLink(link));
        link.setHref(uriBuilder.clone().path(path).build() + query);

        return LinkHeader.format(link);
    }

    private void addHeader(Response.ResponseBuilder responseBuilder, UriBuilder uriBuilder) {
        // concantenate links in a single header with a comma-separated value,
        // which is the canonical form according to:
        // http://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2
        //
        StringBuffer header = new StringBuffer();

        for (Link l : api.getLinks()) {
            header.append(addPath(uriBuilder, l)).append(",");
        }

        header.setLength(header.length() - 1);

        responseBuilder.header("Link", header);
    }

    private Response.ResponseBuilder getResponseBuilder(UriInfo uriInfo) {
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();

        Response.ResponseBuilder responseBuilder = Response.ok();

        addHeader(responseBuilder, uriBuilder);

        return responseBuilder;
    }

    @Override
    public Response head(UriInfo uriInfo) {
        return getResponseBuilder(uriInfo).build();
    }

    @Override
    public Response get(UriInfo uriInfo) {
        return getResponseBuilder(uriInfo).entity(api).build();
    }
}

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

import java.util.UUID;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.BaseResource;
import com.redhat.rhevm.api.model.Link;
import com.redhat.rhevm.api.resource.ActionResource;
import com.redhat.rhevm.api.common.util.LinkHelper;


public class BaseActionResource<R extends BaseResource> implements ActionResource {

    private Action action;
    private R parent;

    public BaseActionResource(UriInfo uriInfo, Action action, R parent) {
        this.action = action;
        this.parent = parent;
        action.setId(UUID.randomUUID().toString());
        addLinks(uriInfo);
    }

    @Override
    public Response get() {
        return Response.ok(action).build();
    }

    @Override
    public Action getAction() {
        return action;
    }

    private String getPath(UriInfo uriInfo) {
        return combine(uriInfo.getBaseUri().getPath(), uriInfo.getPath());
    }

    private void addLink(String rel, String href) {
        Link link = new Link();
        link.setRel(rel);
        link.setHref(href);
        action.getLinks().add(link);
    }

    private void addLinks(UriInfo uriInfo) {
        action.setHref(UriBuilder.fromPath(getPath(uriInfo)).path(action.getId()).build().toString());

        String parentHref = LinkHelper.addLinks(uriInfo, parent).getHref();
        if (parentHref != null) {
            addLink("parent", parentHref);
        }
        addLink("replay", getPath(uriInfo));
    }

    private String combine(String head, String tail) {
        if (head.endsWith("/")) {
            head = head.substring(0, head.length() - 1);
        }
        if (tail.startsWith("/")) {
            tail = tail.substring(1);
        }
        return head + "/" + tail;
    }
}

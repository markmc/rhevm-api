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
package com.redhat.rhevm.api.dummy.resource;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.ActionsBuilder;
import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.resource.HostResource;


public class DummyHostResource extends AbstractDummyResource<Host> implements HostResource {
    /* FIXME: would like to do:
     * private @Context UriInfo uriInfo;
     */

    /**
     * Package-protected ctor, never needs to be instantiated by JAX-RS framework.
     *
     * @param host  encapsulated host
     */
    DummyHostResource(Host host) {
        super(host);
    }

    public Host addLinks(UriBuilder uriBuilder) {
        ActionsBuilder actionsBuilder = new ActionsBuilder(uriBuilder, HostResource.class);
        getModel().setHref(uriBuilder.build().toString());
        getModel().setActions(actionsBuilder.build());
        return getModel();
    }

    /* FIXME: kill uriInfo param, make href auto-generated? */
    @Override
    public Host get(UriInfo uriInfo) {
        return addLinks(uriInfo.getRequestUriBuilder());
    }

    @Override
    public Host update(HttpHeaders headers, UriInfo uriInfo, Host host) {
        validateUpdate(host, headers);
        // update writable fields only
        getModel().setName(host.getName());
        return addLinks(uriInfo.getRequestUriBuilder());
    }

    @Override
    public void approve() {
    }

    @Override
    public void fence() {
    }

    @Override
    public void resume() {
    }

/*
    @Override
    public void connectStorage(String id, String storageDevice) {
    }
*/
}

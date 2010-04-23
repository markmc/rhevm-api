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

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Actions;
import com.redhat.rhevm.api.model.Link;
import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.resource.HostResource;
import com.redhat.rhevm.api.dummy.model.DummyHost;

public class DummyHostResource implements HostResource {
    /* FIXME: would like to do:
     * private @Context UriInfo uriInfo;
     */

    private DummyHost host;

    /**
     * Package-protected ctor, never needs to be instantiated by JAX-RS framework.
     *
     * @param host  encapsulated host
     */
    DummyHostResource(DummyHost host) {
        this.host = host;
    }

    /**
     * Package-level accessor for encapsulated host
     *
     * @return  encapsulated host
     */
    Host getHost() {
        return host;
    }

    public Host addLinks(UriBuilder uriBuilder) {
        host.setLink(new Link("self", uriBuilder.build()));
        host.setActions(new Actions(uriBuilder, HostResource.class));
        return new Host(host);
    }

    /* FIXME: kill uriInfo param, make href auto-generated? */
    @Override
    public Host get(UriInfo uriInfo) {
        return addLinks(uriInfo.getRequestUriBuilder());
    }

    @Override
    public Host update(UriInfo uriInfo, Host host) {
        this.host.update(host);
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

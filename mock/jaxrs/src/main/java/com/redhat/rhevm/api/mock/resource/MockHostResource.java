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
package com.redhat.rhevm.api.mock.resource;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.ActionsBuilder;
import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.resource.HostResource;
import com.redhat.rhevm.api.common.util.JAXBHelper;


public class MockHostResource extends AbstractMockResource<Host> implements HostResource {
    /* FIXME: would like to do:
     * private @Context UriInfo uriInfo;
     */

    /**
     * Package-protected ctor, never needs to be instantiated by JAX-RS framework.
     *
     * @param host  encapsulated host
     */
    MockHostResource(String id) {
        super(id);
    }

    // FIXME: this needs to be atomic
    public void updateModel(Host host) {
        // update writable fields only
        getModel().setName(host.getName());
    }

    public Host addLinks(UriBuilder uriBuilder) {
        Host host = JAXBHelper.clone(OBJECT_FACTORY.createHost(getModel()));

        host.setHref(uriBuilder.build().toString());

        ActionsBuilder actionsBuilder = new ActionsBuilder(uriBuilder, HostResource.class);
        host.setActions(actionsBuilder.build());

        return host;
    }

    /* FIXME: kill uriInfo param, make href auto-generated? */
    @Override
    public Host get(UriInfo uriInfo) {
        return addLinks(uriInfo.getRequestUriBuilder());
    }

    @Override
    public Host update(HttpHeaders headers, UriInfo uriInfo, Host host) {
        validateUpdate(host, headers);
        updateModel(host);
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

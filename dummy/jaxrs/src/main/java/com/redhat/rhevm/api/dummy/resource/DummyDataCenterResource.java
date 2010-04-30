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

import com.redhat.rhevm.api.model.ActionsBuilder;
import com.redhat.rhevm.api.model.DataCenter;
import com.redhat.rhevm.api.resource.DataCenterResource;
import com.redhat.rhevm.api.resource.StorageDomainsResource;
import com.redhat.rhevm.api.dummy.model.DummyDataCenter;

public class DummyDataCenterResource implements DataCenterResource {
    /* FIXME: would like to do:
     * private @Context UriInfo uriInfo;
     */

    private DummyDataCenter dataCenter;
    private DummyDataCenterStorageDomainsResource storageDomains = new DummyDataCenterStorageDomainsResource();

    /**
     * Package-protected ctor, never needs to be instantiated by JAX-RS framework.
     *
     * @param dataCenter  encapsulated DataCenter
     */
    DummyDataCenterResource(DummyDataCenter dataCenter) {
        this.dataCenter = dataCenter;
    }

    /**
     * Package-level accessor for encapsulated DataCenter
     *
     * @return  encapsulated dataCenter
     */
    DummyDataCenter getDataCenter() {
        return dataCenter;
    }

    public DataCenter addLinks(UriBuilder uriBuilder) {
        ActionsBuilder actionsBuilder = new ActionsBuilder(uriBuilder, DataCenterResource.class);
        return dataCenter.getJaxb(uriBuilder, actionsBuilder);
    }

    /* FIXME: kill uriInfo param, make href auto-generated? */
    @Override
    public DataCenter get(UriInfo uriInfo) {
        return addLinks(uriInfo.getRequestUriBuilder());
    }

    @Override
    public DataCenter update(UriInfo uriInfo, DataCenter dataCenter) {
        this.dataCenter.update(dataCenter);
        return addLinks(uriInfo.getRequestUriBuilder());
    }

    @Override
    public StorageDomainsResource getStorageDomainsResource() {
        return storageDomains;
    }
}

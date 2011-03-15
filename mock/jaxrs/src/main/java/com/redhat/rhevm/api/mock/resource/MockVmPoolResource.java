/*
 * Copyright © 2011 Red Hat, Inc.
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

import java.util.concurrent.Executor;

import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.model.VmPool;
import com.redhat.rhevm.api.resource.AssignedPermissionsResource;
import com.redhat.rhevm.api.resource.VmPoolResource;
import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.common.util.JAXBHelper;
import com.redhat.rhevm.api.common.util.LinkHelper;


public class MockVmPoolResource extends AbstractMockResource<VmPool> implements VmPoolResource {
    /* FIXME: would like to do:
     * private @Context UriInfo uriInfo;
     */

    /**
     * Package-protected ctor, never needs to be instantiated by JAX-RS framework.
     *
     * @param pool     encapsulated VmPool
     * @param executor executor used for asynchronous actions
     */
    MockVmPoolResource(String id, Executor executor, UriInfoProvider uriProvider) {
        super(id, executor, uriProvider);
    }

    // FIXME: this needs to be atomic
    public void updateModel(VmPool pool) {
        // update writable fields only
        if (pool.isSetName()) {
            getModel().setName(pool.getName());
        }
        if (pool.isSetDescription()) {
            getModel().setDescription(pool.getDescription());
        }
        if (pool.isSetCluster() && pool.getCluster().isSetId()) {
            getModel().setCluster(new Cluster());
            getModel().getCluster().setId(pool.getCluster().getId());
        }
        if (pool.isSetTemplate() && pool.getTemplate().isSetId()) {
            getModel().setTemplate(new Template());
            getModel().getTemplate().setId(pool.getTemplate().getId());
        }
    }

    public VmPool addLinks() {
        return LinkHelper.addLinks(getUriInfo(), JAXBHelper.clone("vmpool", VmPool.class, getModel()));
    }

    @Override
    public VmPool get() {
        return addLinks();
    }

    @Override
    public VmPool update(VmPool pool) {
        validateUpdate(pool);
        updateModel(pool);
        return addLinks();
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        // TODO Auto-generated method stub
        return null;
    }
}

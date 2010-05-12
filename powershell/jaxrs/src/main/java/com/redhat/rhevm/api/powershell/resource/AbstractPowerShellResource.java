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

import com.redhat.rhevm.api.model.BaseResource;

import com.redhat.rhevm.api.common.resource.AbstractActionableResource;


public abstract class AbstractPowerShellResource<R extends BaseResource> extends AbstractActionableResource<R> {

    protected String id;

    /**
     * For resources that do not maintain their own representation, we
     * cache resource state from last retrieval or update, so that a
     * snapshot of existing state is available for checking immutability
     * constraints on any subsequent updates.
     * Note that the some resource state may change as a result of other actions
     * (for example its status would be updated by the start operation),
     * but that doesn't need to be reflected in the prototype as this is
     * only concerned with the fundamental immutable state of the resource
     * which would not be impacted by an action.
     */

    public AbstractPowerShellResource(String id) {
        this(null, id);
        this.id = id;
    }

    public AbstractPowerShellResource(R model, String id) {
        super(model);
        this.id = id;
    }

    @Override
    public synchronized R getModel() {
        return model == null
               ? model = refreshRepresentation()
               : model;
    }

    /**
     * Refresh representation from arms-length source
     */
    protected R refreshRepresentation() {
        return null;
    }
}

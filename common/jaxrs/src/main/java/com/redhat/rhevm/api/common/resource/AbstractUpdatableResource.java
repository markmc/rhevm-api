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


import com.redhat.rhevm.api.model.BaseResource;
import com.redhat.rhevm.api.model.ObjectFactory;

import com.redhat.rhevm.api.common.util.MutabilityAssertor;
import com.redhat.rhevm.api.common.util.ReflectionHelper;


public abstract class AbstractUpdatableResource<R extends BaseResource> {

    protected static final String[] STRICTLY_IMMUTABLE = {"id"};

    protected final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    protected final R model = newModel();

    /**
     * Create a new instance of the resource
     *
     * @return a newly constructed instance
     */
    protected R newModel() {
        return ReflectionHelper.newModel(this);
    }

    public AbstractUpdatableResource(String id) {
        setId(id);
    }

    /**
     * Validate update from an immutability point of view.
     *
     * @param incoming  the incoming resource representation
     * @param existing  the existing resource representation
     * @throws WebApplicationException wrapping an appropriate response
     * iff an immutability constraint has been broken
     */
    protected void validateUpdate(R incoming) {
        refresh();
        MutabilityAssertor.validateUpdate(getStrictlyImmutable(), incoming, model);
    }

    public void setId(String id) {
        model.setId(id);
    }

    public String getId() {
        return model.getId();
    }

    /**
     * Override this method if any additional resource-specific fields are
     * strictly immutable
     *
     * @return array of strict immutable field names
     */
    protected String[] getStrictlyImmutable() {
        return STRICTLY_IMMUTABLE;
    }

    protected String[] addStrictlyImmutable(String... fields) {
        String[] immutable = new String[STRICTLY_IMMUTABLE.length + fields.length];
        System.arraycopy(STRICTLY_IMMUTABLE, 0, immutable, 0, STRICTLY_IMMUTABLE.length);
        System.arraycopy(fields, 0, immutable, STRICTLY_IMMUTABLE.length, fields.length);
        return immutable;
    }

    /**
     * Refresh the current model state for update validity checking.
     *
     * Override this method if any additional resource-specific fields
     * are strictly immutable by the client but may change in the backend.
     */
    protected void refresh() {}
}

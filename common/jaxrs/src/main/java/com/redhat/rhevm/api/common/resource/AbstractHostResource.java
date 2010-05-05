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

import javax.ws.rs.core.HttpHeaders;

import com.redhat.rhevm.api.model.Host;

import com.redhat.rhevm.api.resource.HostResource;
import com.redhat.rhevm.api.common.util.MutabilityAssertor;

public abstract class AbstractHostResource implements HostResource {

    protected static final String[] STRICTLY_IMMUTABLE = {"id"};

    protected String id;

    public AbstractHostResource(String id) {
        this.id = id;
    }

    /**
     * Validate update from an immutability point of view.
     *
     * @param incoming  the incoming Host representation
     * @param existing  the existing Host representation
     * @param headers   the incoming HTTP headers
     * @throws WebApplicationException wrapping an appropriate response
     * iff an immutability constraint has been broken
     */
    protected void validateUpdate(Host incoming, Host existing, HttpHeaders headers) {
        MutabilityAssertor.validateUpdate(STRICTLY_IMMUTABLE, incoming, existing, headers);
    }
}

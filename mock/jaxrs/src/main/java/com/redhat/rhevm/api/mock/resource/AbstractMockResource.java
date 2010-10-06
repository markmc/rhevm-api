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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import com.redhat.rhevm.api.model.BaseResource;

import com.redhat.rhevm.api.common.resource.AbstractActionableResource;
import com.redhat.rhevm.api.common.resource.UriInfoProvider;


public abstract class AbstractMockResource<R extends BaseResource> extends AbstractActionableResource<R> {

    private static Map<Class<?>, AtomicInteger> counters = new HashMap<Class<?>, AtomicInteger>();

    public AbstractMockResource(String id) {
        super(id);
    }

    public AbstractMockResource(String id, UriInfoProvider uriProvider) {
        super(id);
        this.uriProvider = uriProvider;
    }

    public AbstractMockResource(String id, Executor executor) {
        super(id, executor);
    }

    public AbstractMockResource(String id, Executor executor, UriInfoProvider uriProvider) {
        super(id, executor, uriProvider);
    }

    public static String allocateId(Class<?> clazz) {
        return Integer.toString(getCounter(clazz));
    }

    public synchronized R getModel() {
        return model;
    }

    /**
     * A per-generic-instantiation map of counters.
     *
     * @param   clz the generic type parameter
     * @return  the counter value for that type
     */
    private static int getCounter(Class<?> clz) {
        AtomicInteger i = counters.get(clz);
        if (i == null) {
            i = new AtomicInteger();
            counters.put(clz, i);
        }
        return i.incrementAndGet();
    }
}

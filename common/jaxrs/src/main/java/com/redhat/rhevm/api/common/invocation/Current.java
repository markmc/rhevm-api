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
package com.redhat.rhevm.api.common.invocation;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.interception.PostProcessInterceptor;

@Provider
@ServerInterceptor
public class Current implements PostProcessInterceptor {

    private ThreadLocal<Map<Class<?>, Object>> currents;

    public Current() {
        currents = new ThreadLocal<Map<Class<?>, Object>>();
    }

    @Override
    public void postProcess(ServerResponse response) {
        currents.set(null);
    }

    public <T> void set(T current) {
        getMap().put(current.getClass(), current);
    }

    public <T> T get(Class<T> clz) {
        return clz.cast(getMap().get(clz));
    }

    private Map<Class<?>, Object> getMap() {
        if (currents.get() == null) {
            currents.set(new HashMap<Class<?>, Object>());
        }
        return currents.get();
    }
}

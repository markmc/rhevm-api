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
package com.redhat.rhevm.api.common.util;

import java.util.HashMap;
import java.util.Map;

import com.redhat.rhevm.api.model.Host;

public class QueryTransformer {

    public static Map<Class<?>, QueryTransform> TRANSFORMS = new HashMap<Class<?>, QueryTransform>();

    static {
        TRANSFORMS.put(Host.class, new HostQueryTransform());
    }

    public static String transform(Class<?> clz, String query) {
        if (TRANSFORMS.containsKey(clz)) {
            return TRANSFORMS.get(clz).transform(query);
        } else {
            return query;
        }
    }
}

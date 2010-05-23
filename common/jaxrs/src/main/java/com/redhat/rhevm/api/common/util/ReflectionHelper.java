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
package com.redhat.rhevm.api.common.util;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

import com.redhat.rhevm.api.common.resource.AbstractUpdatableResource;
import com.redhat.rhevm.api.model.BaseResource;

public class ReflectionHelper {

    private static final String GET_ROOT = "get";
    private static final String SET_ROOT = "set";
    private static final String IS_SET_ROOT = "isSet";

    private ReflectionHelper() {}

    @SuppressWarnings("unchecked")
    public static <R extends BaseResource> R newModel(AbstractUpdatableResource<R> resource) {
        R ret = null;
        try {
            ret = ((Class<R>)((ParameterizedType)resource.getClass().getGenericSuperclass()).getActualTypeArguments()[0]).newInstance();
        } catch (Exception e) {
        }
        return ret;
    }

    public static String capitalize(String s) {
        return Character.isLowerCase(s.charAt(0))
               ? s.substring(0, 1).toUpperCase() + s.substring(1)
               : s;
    }

    public static boolean isSet(Object o, String name) {
        Method m = getMethod(o, IS_SET_ROOT + name);
        Object ret = invoke(o, m);
        return ret != null && ret instanceof Boolean && ((Boolean)ret).booleanValue();
    }

    public static boolean different(Object lhs, Object rhs, String name) {
        Method lhsm = getMethod(lhs, GET_ROOT + name);
        Method rhsm = getMethod(rhs, GET_ROOT + name);
        Object lhsr = lhsm != null ? invoke(lhs, lhsm) : null;
        Object rhsr = rhsm != null ? invoke(rhs, rhsm) : null;
        return !(lhsr == null || lhsr.equals(rhsr));
    }

    public static Object invoke(Object o, Method m) {
        Object ret = null;
        try {
            ret = m.invoke(o);
        } catch (Exception e) {
            // InvocationTargetException etc. should never occur
            // as this is a simple getter
        }
        return ret;
    }

    public static boolean set(Object o, String field, String value) {
        boolean success = false;
        String name = SET_ROOT + capitalize(field);
        for (Method m : o.getClass().getMethods()) {
            if (m.getName().equals(name) && isPrimitive(m)) {
                Object arg = isString(m)
                             ? value
                             : isBoolean(m)
                               ? Boolean.parseBoolean(value)
                               : isInteger(m)
                                 ? Integer.parseInt(value)
                                 : null;
                if (arg != null) {
                    try {
                        m.invoke(o, arg);
                        success = true;
                    } catch (Exception e) {
                        // InvocationTargetException etc. should never occur
                        // as this is a simple getter
                    }
                }
                break;
            }
        }
        return success;
    }

    public static Object get(Object o, String field) {
        Object ret = null;
        String name = GET_ROOT + capitalize(field);
        for (Method m : o.getClass().getMethods()) {
            if (m.getName().equals(name)) {
                try {
                    ret = m.invoke(o);
                } catch (Exception e) {
                    // InvocationTargetException etc. should never occur
                    // as this is a simple getter
                }
                break;
            }
        }
        return ret;
    }

    private static boolean isPrimitive(Method m) {
        Class<?>[] params = m.getParameterTypes();
        return params.length == 1
               && (String.class.equals(params[0])
                   || Boolean.TYPE.equals(params[0])
                   || Integer.TYPE.equals(params[0]));
    }

    private static boolean isString(Method m) {
        return String.class.equals(m.getParameterTypes()[0]);
    }

    private static boolean isInteger(Method m) {
        return Integer.TYPE.equals(m.getParameterTypes()[0]);
    }

    private static boolean isBoolean(Method m) {
        return Boolean.TYPE.equals(m.getParameterTypes()[0]);
    }

    private static Method getMethod(Object o, String name) {
        Method ret = null;
        for (Method m : o.getClass().getMethods()) {
            if (m.getName().equals(name)) {
                ret = m;
                break;
            }
        }
        return ret;
    }
}


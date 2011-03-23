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

import java.text.MessageFormat;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.redhat.rhevm.api.model.Fault;

public class EnumValidator {

    private static final String INVALID_ENUM_REASON = "Invalid value";
    private static final String INVALID_ENUM_DETAIL = "{0} is not a member of {1}";

    private static final Response.Status INVALID_ENUM_STATUS = Response.Status.BAD_REQUEST;

    /* Validate that @name is the name of an enum constant from the
     * enum class @clz.
     *
     * @param clz  the enum class
     * @param name the enum constant name; must not be null
     * @return     the enum constant
     * @throws WebApplicationException wrapping an appropriate response
     * iff the @name is invalid
     */
    public static <E extends Enum<E>> E validateEnum(Class<E> clz, String name) {
        return validateEnum(INVALID_ENUM_REASON, INVALID_ENUM_DETAIL, clz, name);
    }

    /* Validate that @name is the name of an enum constant from the
     * enum class @clz.
     *
     * @param reason    the fault reason
     * @param detail    the fault detail
     * @param clz  the enum class
     * @param name the enum constant name; must not be null
     * @return     the enum constant
     * @throws WebApplicationException wrapping an appropriate response
     * iff the @name is invalid
     */
    public static <E extends Enum<E>> E validateEnum(String reason, String detail, Class<E> clz, String name) {
        try {
            return Enum.valueOf(clz, name);
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(response(reason, MessageFormat.format(detail, name, clz.getSimpleName())));
        }
    }

    private static Response response(String reason, String detail) {
        return Response.status(INVALID_ENUM_STATUS).entity(fault(reason, detail)).build();
    }

    private static Fault fault(String reason, String detail) {
        Fault fault = new Fault();
        fault.setReason(reason);
        fault.setDetail(detail);
        return fault;
    }
}

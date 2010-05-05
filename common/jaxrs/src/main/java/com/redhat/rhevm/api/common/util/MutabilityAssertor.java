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

import java.text.MessageFormat;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.redhat.rhevm.api.model.BaseResource;
import com.redhat.rhevm.api.model.Fault;

import static com.redhat.rhevm.api.common.util.ReflectionHelper.capitalize;
import static com.redhat.rhevm.api.common.util.ReflectionHelper.different;
import static com.redhat.rhevm.api.common.util.ReflectionHelper.isSet;

/**
 * Used to assert that fields set on a model type do not conflict with
 * mutability constraints
 */
public class MutabilityAssertor {

    // REVISIT: i18n
    private static final String BROKEN_CONSTRAINT_REASON = "Broken immutability constraint";
    private static final String BROKEN_CONSTRAINT_DETAIL = "Attempt to set immutable field: {0}";

    // REVISIT: is "409 Conflicted" actually the appropriate status here?
    // The idea is to convey a conflict with the fundamental immutable state
    // of the resource, but it also carries connotations of a dirty update
    private static final Response.Status BROKEN_CONSTRAINT_STATUS = Response.Status.CONFLICT;

    /**
     * Impose immutability constraints.
     *
     * @param <T>       representation type
     * @param strict    array of strictly immutable field names
     * @param incoming  incoming representation
     * @param existing  existing representation
     * @return          error Response if appropriate
     */
    public static <T extends BaseResource> Response imposeConstraints(String[] strict, T incoming, T existing, HttpHeaders headers) {
        for (String s: strict) {
            String field = capitalize(s);
            if (isSet(incoming, field) && different(incoming, existing, field)) {
                Fault fault = new Fault();
                fault.setReason(BROKEN_CONSTRAINT_REASON);
                fault.setDetail(MessageFormat.format(BROKEN_CONSTRAINT_DETAIL, s));
                return buildFaultResponse(headers, fault);
            }
        }
        return null;
    }

    private static Response buildFaultResponse(HttpHeaders headers, Fault fault) {
        // an apparent bug in RESTeasy results in the entity being marshalled
        // as yaml regardless of the normal content negotiation rules, hence
        // the explicit setting of the Content-Type header on the response
        //
        return Response.status(BROKEN_CONSTRAINT_STATUS)
                       .header("Content-Type", getContentType(headers))
                       .entity(fault)
                       .build();
    }

    private static String getContentType(HttpHeaders headers) {
        return headers.getAcceptableMediaTypes() != null && headers.getAcceptableMediaTypes().size() > 1
              ? headers.getAcceptableMediaTypes().get(0).toString()
              : headers.getMediaType() != null
                ? headers.getMediaType().toString()
                : MediaType.APPLICATION_XML;
    }
}

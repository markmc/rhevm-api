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
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.redhat.rhevm.api.model.Fault;

import static com.redhat.rhevm.api.common.util.ReflectionHelper.capitalize;
import static com.redhat.rhevm.api.common.util.ReflectionHelper.get;
import static com.redhat.rhevm.api.common.util.ReflectionHelper.isSet;

/**
 * Used to validate that the required fields are set on a user-provided
 * model instance
 */
public class CompletenessAssertor {

    // REVISIT: i18n
    private static final String INCOMPLETE_PARAMS_REASON = "Incomplete parameters";
    private static final String INCOMPLETE_PARAMS_DETAIL = "{0} {1} required for {2}";
    private static final String ALTERNATIVE = "\\|";

    private static final Response.Status INCOMPLETE_PARAMS_STATUS = Response.Status.BAD_REQUEST;

    /**
     * Validate presence of required parameters.
     * Note the model type is java.lang.Object as opposed to a generic
     * <T extends BaseResource> in order to accommodate parameters types
     * such as Action.
     *
     * @param model     the incoming representation
     * @param required  the required field names
     * @throws WebApplicationException wrapping an appropriate response
     * iff a required parameter is missing
     */
    public static void validateParameters(Object model, String... required) {
        Response error = assertRequired(model, required);
        if (error != null) {
            throw new WebApplicationException(error);
        }
    }

    /**
     * Validate presence of required parameters.
     *
     * @param model     the incoming representation
     * @param required  the required field names
     * @return          error Response if appropriate
     */
    private static Response assertRequired(Object model, String... required) {
        List<String> missing = new ArrayList<String>();
        for (String r : required) {
            if (topLevel(r)) {
                if (!assertFields(model, subField(r))) {
                    missing.add(r);
                }
            } else if (isList(model, superField(r))) {
                for (Object item : asList(model, superField(r))) {
                    if (!assertFields(item, subField(r))) {
                        missing.add(r);
                    }
                }
            } else {
                boolean found = false;
                for (String superField : superField(r).split(ALTERNATIVE)) {
                    found = found || (isSet(model, capitalize(superField)) && assertFields(model, superField, subField(r)));
                }
                if (!found) {
                    missing.add(r);
                }
            }
        }

        Response response = null;
        if (!missing.isEmpty()) {
            StackTraceElement[] trace = new Throwable().getStackTrace();
            Fault fault = new Fault();
            fault.setReason(INCOMPLETE_PARAMS_REASON);
            fault.setDetail(MessageFormat.format(INCOMPLETE_PARAMS_DETAIL,
                                                 model.getClass().getSimpleName(),
                                                 missing,
                                                 trace[2].getMethodName()));
            response = Response.status(INCOMPLETE_PARAMS_STATUS)
                               .entity(fault)
                               .build();
        }

        return response;
    }

    private static boolean assertFields(Object model, String fields) {
        return assertFields(model, null, fields);
    }

    private static boolean assertFields(Object model, String superField, String subFields) {
        String[] splitFields = subFields.split(ALTERNATIVE);
        boolean found = false;
        for (String subField : splitFields) {
            found = found || isSet(superField != null ? get(model, superField) : model, capitalize(subField));
        }
        return found;
    }

    private static boolean topLevel(String required) {
        return required.indexOf(".") == -1;
    }

    private static String superField(String required) {
        return capitalize(required.substring(0, required.indexOf(".")));
    }

    private static String subField(String required) {
        return required.substring(required.indexOf(".") + 1);
    }

    @SuppressWarnings("unchecked")
    private static boolean isList(Object model, String superField) {
        return isSet(model, superField)
               && isSet(get(model, superField), superField)
               && get(get(model, superField), superField) instanceof List;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> asList(Object model, String superField) {
        return (List<Object>)get(get(model, superField), superField);
    }
}

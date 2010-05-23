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
package com.redhat.api.mock.util;

import com.redhat.rhevm.api.common.util.ReflectionHelper;
import com.redhat.rhevm.api.model.BaseResource;

/**
 * Extremely simple-minded query evaluator limited to a single logical
 * predicate testing equality with a primitive integral or boolean field,
 * or a regex-match against a string field.
 *
 * @param <R> Resource type
 */
public class SimpleQueryEvaluator<R extends BaseResource> implements QueryEvaluator<R> {

    /**
     * Evaluate whether a resource satisfies a given constraint.
     *
     * @param resource   the resource under test
     * @param constraint the constraint being impose
     * @return true iff the constraint is satisfied
     */
    @Override
    public boolean satisfies(R resource, String constraint) {
        boolean satisfied = false;
        try {
            constraint = constraint.substring(constraint.indexOf(":") + 1).trim();
            String[] tokens = constraint.split("=");
            Object value = ReflectionHelper.get(resource, tokens[0]);
            satisfied =  value instanceof String
                         ? ((String)value).matches(tokens[1].replaceAll("\\*", ".*"))
                         : value instanceof Boolean
                           ? ((Boolean)value).equals(Boolean.parseBoolean(constraint))
                           : value instanceof Integer
                             ? ((Integer)value).equals(Integer.parseInt(constraint))
                             : false;
        } catch (Exception e) {
            // any parse exception implies a mismatch
            e.printStackTrace();
        }
        return satisfied;
    }

}

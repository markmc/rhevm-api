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

import javax.ws.rs.core.UriInfo;

import com.redhat.api.mock.util.QueryEvaluator;
import com.redhat.rhevm.api.common.util.QueryHelper;
import com.redhat.rhevm.api.model.BaseResource;

public class AbstractMockQueryableResource<R extends BaseResource> extends AbstractMockCollectionResource {
    private QueryEvaluator<R> evaluator;

    protected AbstractMockQueryableResource(QueryEvaluator<R> evaluator) {
        this.evaluator = evaluator;
    }

    protected boolean filter(R resource, UriInfo uriInfo, Class<?> clz) {
        String constraint = QueryHelper.getConstraint(uriInfo, clz);
        return constraint != null ? evaluator.satisfies(resource, constraint) : true;
    }
}

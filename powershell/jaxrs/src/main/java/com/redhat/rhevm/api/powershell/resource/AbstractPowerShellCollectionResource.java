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
package com.redhat.rhevm.api.powershell.resource;

import java.util.concurrent.Executor;

import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.common.resource.AbstractUpdatableResource;
import com.redhat.rhevm.api.common.util.QueryHelper;
import com.redhat.rhevm.api.common.util.ReapedMap;
import com.redhat.rhevm.api.model.BaseResource;

public abstract class AbstractPowerShellCollectionResource<R extends BaseResource,
                                                           U extends AbstractUpdatableResource<R>> {
    private ReapedMap<String, U> resources;
    private Executor executor;
    private final static String SEARCH_TEXT = " -searchtext ";
    private final static String QUOTE = "'";

    public AbstractPowerShellCollectionResource() {
        resources = new ReapedMap<String, U>();
    }

    protected U getSubResource(String id) {
        synchronized (resources) {
            U ret = resources.get(id);
            if (ret == null) {
                ret = createSubResource(id);
                resources.put(id, ret);
                resources.reapable(id);
            }
            return ret;
        }
    }

    protected void removeSubResource(String id) {
        synchronized (resources) {
            resources.remove(id);
        }
    }

    protected String getSelectCommand(String root, UriInfo uriInfo, Class<?> collectionType) {
        String ret = root;
        String constraint = QueryHelper.getConstraint(uriInfo, collectionType);
        if (constraint != null) {
            ret = new StringBuffer(root).append(SEARCH_TEXT)
                                        .append(quoted(constraint))
                                        .toString();
        }
        return ret;
    }

    private static StringBuffer quoted(String s) {
        return new StringBuffer(QUOTE).append(s).append(QUOTE);
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    protected abstract U createSubResource(String id);
}

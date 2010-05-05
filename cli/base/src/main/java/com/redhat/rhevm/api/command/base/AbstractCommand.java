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
package com.redhat.rhevm.api.command.base;

import java.util.Collection;
import java.util.List;

import org.apache.felix.karaf.shell.console.OsgiCommandSupport;

import com.redhat.rhevm.api.command.base.BaseClient;
import com.redhat.rhevm.api.model.BaseResource;
import com.redhat.rhevm.api.model.Link;

/**
 * Command base
 */
public abstract class AbstractCommand extends OsgiCommandSupport {

    protected String baseUrl;
    protected BaseClient client;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public BaseClient getClient() {
        return client;
    }

    public void setClient(BaseClient client) {
        this.client = client;
    }

    protected <T extends BaseResource> T getResource(List<T> collection, String name) {
        // need to do better than linear search for large collections
        for (T resource : collection) {
            if (name.equals(resource.getName())) {
                return resource;
            }
        }
        if (collection.size() > 0) {
            System.err.println(name + " is unknown, use tab-completion to see a list of valid targets");
        }
        return null;
    }

    protected Link getLink(BaseResource resource, String rel) {
        Link ret = null;
        Collection<Link> links = resource.getActions().getLinks();
        for (Link l : links) {
           if (l.getRel().equals(rel)) {
               ret = l;
               break;
           }
        }
        return ret;
    }
}

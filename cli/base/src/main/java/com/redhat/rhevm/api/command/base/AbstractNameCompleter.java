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

import java.util.List;

import org.apache.felix.karaf.shell.console.Completer;
import org.apache.felix.karaf.shell.console.completer.StringsCompleter;

import com.redhat.rhevm.api.command.base.BaseClient;
import com.redhat.rhevm.api.model.BaseResource;

public abstract class AbstractNameCompleter implements Completer {

    protected BaseClient client;

    public void setClient(BaseClient client) {
        this.client = client;
    }

    public int complete(final String buffer, final int cursor, final List candidates) {
        StringsCompleter delegate = new StringsCompleter();
        try {
            List<? extends BaseResource> collection = getCollection();
            for (BaseResource resource : collection) {
                delegate.getStrings().add(resource.getName());
            }
        } catch (Exception e) {
            // Substantive exceptions dealt within the BaseClient
        }
        return delegate.complete(buffer, cursor, candidates);
    }

    protected abstract List<? extends BaseResource> getCollection() throws Exception;
}

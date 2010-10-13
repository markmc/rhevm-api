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
package com.redhat.rhevm.api.model;

import java.net.URI;
import java.lang.reflect.Method;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;

import com.redhat.rhevm.api.model.Actions;
import com.redhat.rhevm.api.model.Link;

public class ActionsBuilder {

    private UriBuilder uriBuilder;
    private Class<?> service;

    public ActionsBuilder(UriBuilder uriBuilder, Class<?> service) {
        this.uriBuilder = uriBuilder;
        this.service = service;
    }

    public Actions build() {
        Actions actions = new Actions();

        for (Method method : service.getMethods()) {
            Path path = method.getAnnotation(Path.class);
            Actionable actionable = method.getAnnotation(Actionable.class);

            if (actionable != null && path != null) {
                URI uri = uriBuilder.clone().path(path.value()).build();

                Link link = new Link();
                link.setRel(path.value());
                link.setHref(uri.toString());

                actions.getLinks().add(link);
            }
        }

        return actions;
    }
}

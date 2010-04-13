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
package com.redhat.rhevm.api;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.lang.reflect.Method;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import javax.ws.rs.core.UriBuilder;

/* FIXME: could probably drop @XmlAccessorType */

@XmlRootElement(name = "action")
@XmlAccessorType(XmlAccessType.NONE)
public class Actions
{
	public Actions() {
	}

	public Actions(UriBuilder uriBuilder, Class<?> service) {
		for (Method method : service.getMethods()) {
			if (method.getAnnotation(Action.class) != null) {
				URI uri = uriBuilder.clone().path(method.getName()).build();

				links.add(new Link(method.getName(), uri));
			}
		}
	}

	@XmlElementRef
	public Collection<Link> getLinks() {
		return links;
	}
	public void setLinks(Collection<Link> links) {
		this.links = links;
	}
	protected Collection<Link> links = new ArrayList<Link>();
}

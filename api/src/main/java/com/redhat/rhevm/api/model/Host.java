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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.resteasy.annotations.providers.jaxb.json.BadgerFish;

/* FIXME: switch from BadgerFish to the Mapped convention */
/* FIXME: could probably drop @XmlAccessorType */

@BadgerFish
@XmlRootElement(name = "host")
@XmlAccessorType(XmlAccessType.NONE)
public class Host {
    /* FIXME:
     * This is a giant hack so that JAX-B marshalls the
     * superclass rather than the subclass - e.g. we want the type
     * to be 'Host' not 'DummyHost' when it is passed to jyaml
     */
    public Host(Host host) {
        link         = host.link;
        id           = host.id;
        name         = host.name;
        actions      = host.actions;
        address      = host.address;
        rootPassword = host.rootPassword;
    }

    public Host() {
    }

    /* FIXME: can we make this attritbute be auto-generated
     *        somehow?
     */
    @XmlElementRef
    public Link getLink() {
        return link;
    }
    public void setLink(Link link) {
        this.link = link;
    }
    protected Link link;

    @XmlElement(name = "id")
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    protected String id;

    @XmlElement(name = "name")
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    protected String name;

    @XmlElement(name = "actions")
    public Actions getActions() {
        return actions;
    }
    public void setActions(Actions actions) {
        this.actions = actions;
    }
    protected Actions actions;

    @XmlElement(name = "address")
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    protected String address;

    @XmlElement(name = "root_password")
    public String getRootPassword() {
        return rootPassword;
    }
    public void setRootPassword(String rootPassword) {
        this.rootPassword = rootPassword;
    }
    protected String rootPassword;
}

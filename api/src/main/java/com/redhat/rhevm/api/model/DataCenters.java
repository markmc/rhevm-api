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

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.resteasy.annotations.providers.jaxb.json.BadgerFish;

/* FIXME: switch from BadgerFish to the Mapped convention */
/* FIXME: could probably drop @XmlAccessorType */

@BadgerFish
@XmlRootElement(name = "datacenters")
@XmlAccessorType(XmlAccessType.NONE)
public class DataCenters {
    @XmlElementRef
    public Collection<DataCenter> getDataCenters() {
        return datacenters;
    }
    public void setDataCenters(Collection<DataCenter> datacenters) {
        this.datacenters = datacenters;
    }
    protected Collection<DataCenter> datacenters = new ArrayList<DataCenter>();
}

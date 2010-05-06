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
package com.redhat.rhevm.api.dummy.model;

import javax.ws.rs.core.UriBuilder;
import com.redhat.rhevm.api.model.ActionsBuilder;
import com.redhat.rhevm.api.model.DataCenter;
import com.redhat.rhevm.api.model.Link;

public class DummyDataCenter {

    public DataCenter jaxb = new DataCenter();

    private static int counter = 0;

    public DummyDataCenter() {
        jaxb.setId(Integer.toString(++counter));
    }

    public DummyDataCenter(DataCenter dataCenter) {
        this();
        update(dataCenter);
    }

    public void update(DataCenter dataCenter) {
        // update writable fields only
        jaxb.setName(dataCenter.getName());
        jaxb.setStorageType(dataCenter.getStorageType());
    }

    public DataCenter getJaxb(UriBuilder uriBuilder, ActionsBuilder actionsBuilder) {
        jaxb.setHref(uriBuilder.build().toString());
        jaxb.setActions(actionsBuilder.build());
        return jaxb;
    }
}

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
import com.redhat.rhevm.api.model.Attachment;
import com.redhat.rhevm.api.model.DataCenter;

public class DummyAttachment {

    public Attachment jaxb = new Attachment();

    public DummyAttachment(Attachment attachment) {
        update(attachment);
    }

    public void update(Attachment attachment) {
        // update writable fields only
        if (attachment.getDataCenter() != null) {
            DataCenter dataCenter = new DataCenter();

            // we're only interested in its id
            dataCenter.setId(attachment.getDataCenter().getId());

            jaxb.setDataCenter(dataCenter);
        }
    }

    public Attachment getJaxb(UriBuilder uriBuilder, ActionsBuilder actionsBuilder) {
        jaxb.setHref(uriBuilder.build().toString());
        jaxb.setActions(actionsBuilder.build());
        return jaxb;
    }
}

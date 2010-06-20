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
package com.redhat.rhevm.api.command.datacenters;

import com.redhat.rhevm.api.model.Attachment;
import com.redhat.rhevm.api.model.DataCenter;

import com.redhat.rhevm.api.command.base.VerboseDisplay;

public class DataCenterVerboseDisplay implements VerboseDisplay<DataCenter> {
    @Override
    public void expand(DataCenter model) {
        if (model.isSetStorageType()) {
            System.out.println("  storage type: " + model.getStorageType());
        }
        if (model.isSetAttachments() && model.getAttachments().getAttachments().size() > 0) {
            System.out.println("  Attachments: ");
            for (Attachment attachment : model.getAttachments().getAttachments()) {
                if (attachment.isSetMaster()) {
                    System.out.println("    master: " + attachment.isMaster());
                }
                if (attachment.isSetDataCenter()) {
                    System.out.println("    data center: " + attachment.getDataCenter().getName());
                }
                if (attachment.isSetStorageDomain()) {
                    System.out.println("    storage domain: " + attachment.getStorageDomain().getName());
                }
                if (attachment.isSetStatus()) {
                    System.out.println("    storage domain status: " + attachment.getStatus());
                }
                System.out.println();
            }
        }
    }
}

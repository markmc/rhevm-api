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
package com.redhat.rhevm.api.command.storagedomains;

import com.redhat.rhevm.api.model.StorageDomain;

import com.redhat.rhevm.api.command.base.VerboseDisplay;

public class StorageDomainVerboseDisplay implements VerboseDisplay<StorageDomain> {
    @Override
    public void expand(StorageDomain model) {
        if (model.isSetMaster()) {
            System.out.println("  master: " + model.isMaster());
        }
        if (model.isSetStatus()) {
            System.out.println("  status: " + model.getStatus());
        }
        if (model.isSetType()) {
            System.out.println("  type: " + model.getType());
        }
        if (model.isSetStorage()) {
            System.out.println("  storage type: " + model.getStorage().getType());
            if (model.getStorage().isSetAddress()) {
                System.out.println("  storage address: " + model.getStorage().getAddress());
            }
            if (model.getStorage().isSetPath()) {
                System.out.println("  storage path: " + model.getStorage().getPath());
            }
            if (model.getStorage().isSetLUN()) {
                System.out.println("  storage LUN: " + model.getStorage().getLUN());
            }
            if (model.getStorage().isSetIP()) {
                System.out.println("  storage IP: " + model.getStorage().getIP());
            }
            if (model.getStorage().isSetPort()) {
                System.out.println("  storage port: " + model.getStorage().getPort());
            }
        }
    }
}

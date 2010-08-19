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

import com.redhat.rhevm.api.model.LogicalUnit;
import com.redhat.rhevm.api.model.Storage;
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
            Storage storage = model.getStorage();
            System.out.println("  storage type: " + storage.getType());
            if (storage.isSetAddress()) {
                System.out.println("  storage address: " + storage.getAddress());
            }
            if (storage.isSetPath()) {
                System.out.println("  storage path: " + storage.getPath());
            }
            if (storage.isSetLogicalUnits()) {
                System.out.println("  Logical Units:");
                for (LogicalUnit lu : storage.getLogicalUnits()) {
                    if (lu.isSetId()) {
                        System.out.println("    ID: " + lu.getId());
                    }
                    if (lu.isSetAddress()) {
                        System.out.println("    Portal: " + lu.getAddress());
                    }
                    if (lu.isSetTarget()) {
                        System.out.println("    Target: " + lu.getTarget());
                    }
                    System.out.println();
                }
            }
        }
    }
}

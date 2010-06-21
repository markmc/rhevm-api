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
package com.redhat.rhevm.api.command.vmpools;

import com.redhat.rhevm.api.model.VmPool;

import com.redhat.rhevm.api.command.base.VerboseDisplay;

public class VmPoolVerboseDisplay implements VerboseDisplay<VmPool> {
    @Override
    public void expand(VmPool model) {
        if (model.isSetSize()) {
            System.out.println("  size: " + model.getSize());
        }
        if (model.isSetCluster()) {
            if (model.getCluster().isSetName()) {
                System.out.println("  cluster: " + model.getCluster().getName());
            } else if (model.getCluster().isSetId()) {
                System.out.println("  cluster ID: " + model.getCluster().getId());
            }
        }
        if (model.isSetTemplate()) {
            if (model.getTemplate().isSetName()) {
                System.out.println("  template: " + model.getTemplate().getName());
            } else if (model.getTemplate().isSetId()) {
                System.out.println("  template ID: " + model.getTemplate().getId());
            }
        }
    }
}

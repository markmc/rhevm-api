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
package com.redhat.rhevm.api.command.vms;

import com.redhat.rhevm.api.model.VM;

import com.redhat.rhevm.api.command.base.VerboseDisplay;

public class VmVerboseDisplay implements VerboseDisplay<VM> {
    @Override
    public void expand(VM model) {
        if (model.isSetStatus()) {
            System.out.println("  status: " + model.getStatus());
        }
        if (model.isSetMemory()) {
            System.out.println("  memory: " + model.getMemory());
        }
        if (model.isSetCluster()) {
            System.out.println("  cluster: " + model.getCluster().getName());
        }
        if (model.isSetTemplate()) {
            System.out.println("  template: " + model.getTemplate().getName());
        }
        if (model.isSetCpu()) {
            System.out.println("  CPU: " + model.getCpu().getId());
        }
        if (model.isSetOs() && model.getOs().isSetBoot()) {
            System.out.println("  OS boot: " + model.getOs().getBoot());
        }
        if (model.isSetVmPool()) {
            System.out.println("  VM pool: " + model.getVmPool().getName());
        }

    }
}

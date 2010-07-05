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
package com.redhat.rhevm.api.command.templates;

import com.redhat.rhevm.api.model.OperatingSystem;
import com.redhat.rhevm.api.model.Template;

import com.redhat.rhevm.api.command.base.VerboseDisplay;

public class TemplateVerboseDisplay implements VerboseDisplay<Template> {
    @Override
    public void expand(Template model) {
        if (model.isSetStatus()) {
            System.out.println("  status: " + model.getStatus());
        }
        if (model.isSetMemory()) {
            System.out.println("  memory: " + Math.floor(model.getMemory() / (1024 * 1024)) + " Mb");
        }
        if (model.isSetCluster()) {
            if (model.getCluster().isSetName()) {
                System.out.println("  cluster: " + model.getCluster().getName());
            } else if (model.getCluster().isSetId()) {
                System.out.println("  cluster ID: " + model.getCluster().getId());
            }
        }
        if (model.isSetCpu()) {
            if (model.getCpu().isSetId()) {
                System.out.println("  CPU: " + model.getCpu().getId());
            }
            if (model.getCpu().isSetTopology()) {
                if (model.getCpu().getTopology().isSetCores()) {
                    System.out.println("  CPU cores: " + model.getCpu().getTopology().getCores());
                }
                if (model.getCpu().getTopology().isSetSockets()) {
                    System.out.println("  CPU sockets: " + model.getCpu().getTopology().getSockets());
                }
            }
        }
        if (model.isSetOs() && model.getOs().isSetBoot()) {
            System.out.print("  boot sequence:");
            for (OperatingSystem.Boot boot : model.getOs().getBoot()) {
                System.out.print(" " + boot.getDev().toString());
            }
            System.out.println();
        }
    }
}

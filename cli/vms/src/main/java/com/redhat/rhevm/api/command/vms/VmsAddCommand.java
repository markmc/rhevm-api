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

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;

import com.redhat.rhevm.api.command.base.AbstractAddCommand;
import com.redhat.rhevm.api.model.BootDevice;
import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.CPU;
import com.redhat.rhevm.api.model.CpuTopology;
import com.redhat.rhevm.api.model.OperatingSystem;
import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.model.VM;

/**
 * Add a new VM.
 */
@Command(scope = "vms", name = "add", description = "Add a new Virtual Machine")
public class VmsAddCommand extends AbstractAddCommand<VM> {

    @Argument(index = 0, name = "name", description = "Name of the VM to add", required = true, multiValued = false)
    protected String name;

    @Option(name = "-t", aliases = { "--template" }, description = "VM template name", required = true, multiValued = false)
    private String template;

    @Option(name = "-c", aliases = { "--cluster" }, description = "Cluster name", required = true, multiValued = false)
    private String cluster;

    @Option(name = "-m", aliases = { "--memory" }, description = "Memory size in Mb", required = false, multiValued = false)
    private int memory = -1;

    @Option(name = "-s", aliases = { "--sockets" }, description = "Number of sockets", required = false, multiValued = false)
    private int sockets = -1;

    @Option(name = "-r", aliases = { "--cores" }, description = "Number of CPUs per socket", required = false, multiValued = false)
    private int cores = -1;

    @Option(name = "-b", aliases = { "--boot" }, description = "Default boot sequence", required = false, multiValued = false)
    private String boot;

    protected Object doExecute() throws Exception {
        display(doAdd(getModel(), VM.class, "vms", "vm"));
        return null;
    }

    protected VM getModel() {
        VM model = new VM();
        model.setName(name);
        model.setTemplate(new Template());
        model.getTemplate().setName(template);
        model.setCluster(new Cluster());
        model.getCluster().setName(cluster);
        if (memory != -1) {
            model.setMemory(memory * 1024 * 1024L);
        }
        if (sockets != -1 || cores != -1) {
            model.setCpu(new CPU());
            model.getCpu().setTopology(new CpuTopology());
            if (sockets != -1) {
                model.getCpu().getTopology().setSockets(sockets);
            }
            if (cores != -1) {
                model.getCpu().getTopology().setCores(cores);
            }
        }
        if (boot != null) {
            model.setOs(getOs());
        }
        return model;
    }

    private OperatingSystem getOs() {
        OperatingSystem os = new OperatingSystem();
        for (int i = 0; i < boot.length(); i++) {
            char c = boot.charAt(i);
            OperatingSystem.Boot boot = new OperatingSystem.Boot();

            switch (c) {
            case 'C':
                boot.setDev(BootDevice.HD);
                break;
            case 'D':
                boot.setDev(BootDevice.CDROM);
                break;
            case 'N':
                boot.setDev(BootDevice.NETWORK);
                break;
            default:
                break;
            }

            if (boot.isSetDev()) {
                os.getBoot().add(boot);
            }
        }
        return os;
    }
}


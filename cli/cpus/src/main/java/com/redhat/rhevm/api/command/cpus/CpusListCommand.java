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
package com.redhat.rhevm.api.command.cpus;

import java.util.List;

import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;

import com.redhat.rhevm.api.command.base.AbstractCommand;

import com.redhat.rhevm.api.model.Capabilities;
import com.redhat.rhevm.api.model.CPU;
import com.redhat.rhevm.api.model.CPUs;

import static com.redhat.rhevm.api.command.base.BaseClient.pad;

/**
 * Displays the Cpus
 */
@Command(scope = "cpus", name = "list", description = "Lists CPUs.")
public class CpusListCommand extends AbstractCommand {

    protected Object doExecute() throws Exception {
        CPUs cpus = client.getRel("cpus", Capabilities.class).getCPUs();
        if (cpus == null) {
            return null;
        }
        List<CPU> collection = cpus.getCPUs();

        int i = 0, widestId = 0;
        for (CPU resource : collection) {
            if (resource.getId() != null && resource.getId().length() > widestId) {
                widestId = resource.getId().length();
            }
        }
        for (CPU resource : collection) {
            System.out.println(pad(resource.getId(), widestId)
                               + pad(Integer.toString(resource.getLevel()), 0)
                               + resource.getFlags().getFlags());
        }
        return null;
    }

    private String value(String f) {
        return f != null ? f : "";
    }
}

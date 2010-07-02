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
package com.redhat.rhevm.api.powershell.model;

import java.util.ArrayList;
import java.util.HashMap;

import com.redhat.rhevm.api.model.BootDevice;
import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.CPU;
import com.redhat.rhevm.api.model.CpuTopology;
import com.redhat.rhevm.api.model.OperatingSystem;
import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.model.TemplateStatus;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;


public class PowerShellTemplate extends Template {

    private static TemplateStatus parseStatus(HashMap<String,String> props, String key) {
        String s = props.get(key);
        if (s == null) return null;
        else if (s.equals("Illegal")) return TemplateStatus.ILLEGAL;
        else if (s.equals("Locked"))  return TemplateStatus.LOCKED;
        else if (s.equals("OK"))      return TemplateStatus.OK;
        else return null;
    }

    public static ArrayList<PowerShellTemplate> parse(String output) {
        ArrayList<HashMap<String,String>> templatesProps = PowerShellUtils.parseProps(output);
        ArrayList<PowerShellTemplate> ret = new ArrayList<PowerShellTemplate>();

        for (HashMap<String,String> props : templatesProps) {
            PowerShellTemplate template = new PowerShellTemplate();

            template.setId(props.get("templateid"));
            template.setName(props.get("name"));
            template.setDescription(props.get("description"));
            template.setMemory(Long.parseLong(props.get("memsizemb")) * 1024 * 1024);

            TemplateStatus status = parseStatus(props, "status");
            if (status != null) {
                template.setStatus(status);
            }

            CpuTopology topo = new CpuTopology();
            topo.setSockets(Integer.parseInt(props.get("numofsockets")));
            topo.setCores(Integer.parseInt(props.get("numofcpuspersocket")));
            CPU cpu = new CPU();
            cpu.setTopology(topo);
            template.setCpu(cpu);

            OperatingSystem os = new OperatingSystem();
            PowerShellVM.parseBootDevices(os, props.get("defaultbootsequence"));
            template.setOs(os);

            Cluster cluster = new Cluster();
            cluster.setId(props.get("hostclusterid"));
            template.setCluster(cluster);

            ret.add(template);
        }

        return ret;
    }
}

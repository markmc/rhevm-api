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

import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.model.VmPool;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;

public class PowerShellVmPool {

    public static ArrayList<VmPool> parse(String output) {
        ArrayList<HashMap<String,String>> poolsProps = PowerShellUtils.parseProps(output);
        ArrayList<VmPool> ret = new ArrayList<VmPool>();

        for (HashMap<String,String> props : poolsProps) {
            VmPool pool = new VmPool();

            pool.setId(props.get("vmpoolid"));
            pool.setName(props.get("name"));
            pool.setDescription(props.get("description"));

            Cluster cluster = new Cluster();
            cluster.setName(props.get("cluster"));
            pool.setCluster(cluster);

            Template template = new Template();
            template.setName(props.get("template"));
            pool.setTemplate(template);

            ret.add(pool);
        }

        return ret;
    }
}

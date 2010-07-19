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
package com.redhat.rhevm.api.powershell.resource;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.model.VmPool;
import com.redhat.rhevm.api.resource.VmPoolResource;
import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.powershell.model.PowerShellVmPool;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;


public class PowerShellVmPoolResource extends AbstractPowerShellActionableResource<VmPool> implements VmPoolResource {

    public PowerShellVmPoolResource(String id, Executor executor, PowerShellPoolMap shellPools) {
        super(id, executor, shellPools);
    }

    public static ArrayList<VmPool> runAndParse(PowerShellCmd shell, String command) {
        return PowerShellVmPool.parse(PowerShellCmd.runCommand(shell, command));
    }

    public static VmPool runAndParseSingle(PowerShellCmd shell, String command) {
        ArrayList<VmPool> pools = runAndParse(shell, command);

        return !pools.isEmpty() ? pools.get(0) : null;
    }

    /* Map the template name to template ID on a VM pool, since
     * the powershell output only includes the template name.
     *
     * @param shell the shell to run the command
     * @param pool  the VM pool to modify
     * @return      the modified VM pool
     */
    private static VmPool lookupTemplateId(PowerShellCmd shell, VmPool pool) {
        StringBuilder buf = new StringBuilder();

        buf.append("select-template -searchtext ");
        buf.append(PowerShellUtils.escape("name = " + pool.getTemplate().getName()));

        Template template = new Template();
        template.setId(PowerShellTemplateResource.runAndParseSingle(shell, buf.toString()).getId());
        pool.setTemplate(template);

        return pool;
    }

    /* Map the cluster name to cluster ID on a VM pool, since
     * the powershell output only includes the cluster name.
     *
     * @param shell the shell to run the command
     * @param pool  the VM pool to modify
     * @return      the modified VM pool
     */
    private static VmPool lookupClusterId(PowerShellCmd shell, VmPool pool) {
        StringBuilder buf = new StringBuilder();

        buf.append("select-cluster -searchtext ");
        buf.append(PowerShellUtils.escape("name = " + pool.getCluster().getName()));

        Cluster cluster = new Cluster();
        cluster.setId(PowerShellClusterResource.runAndParseSingle(shell, buf.toString()).getId());
        pool.setCluster(cluster);

        return pool;
    }

    public static VmPool addLinks(PowerShellCmd shell, VmPool pool) {
        pool = lookupClusterId(shell, pool);

        if (pool.getTemplate() != null) {
            pool = lookupTemplateId(shell, pool);
        }

        return LinkHelper.addLinks(pool);
    }

    @Override
    public VmPool get(UriInfo uriInfo) {
        return addLinks(getShell(), runAndParseSingle(getShell(), "get-vmpool -vmpoolid " + PowerShellUtils.escape(getId())));
    }

    @Override
    public VmPool update(UriInfo uriInfo, VmPool pool) {
        validateUpdate(pool);

        StringBuilder buf = new StringBuilder();

        buf.append("$v = get-vmpool " + PowerShellUtils.escape(getId()) + "\n");

        if (pool.getName() != null) {
            buf.append("$v.name = " + PowerShellUtils.escape(pool.getName()) + "\n");
        }
        if (pool.getDescription() != null) {
            buf.append("$v.description = " + PowerShellUtils.escape(pool.getDescription()) + "\n");
        }
        if (pool.getSize() != null) {
            buf.append("$v.vmcount = " + pool.getSize() + "\n");
        }

        buf.append("update-vmpool -vmpoolobject $v");

        PowerShellCmd cmd = getShell();
        return addLinks(cmd, runAndParseSingle(getShell(), buf.toString()));
    }
}

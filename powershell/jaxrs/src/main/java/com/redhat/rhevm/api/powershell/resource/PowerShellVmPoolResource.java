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

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.model.VmPool;
import com.redhat.rhevm.api.resource.VmPoolResource;
import com.redhat.rhevm.api.common.resource.AbstractActionableResource;
import com.redhat.rhevm.api.powershell.model.PowerShellVmPool;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;


public class PowerShellVmPoolResource extends AbstractActionableResource<VmPool> implements VmPoolResource {

    public PowerShellVmPoolResource(String id, Executor executor) {
        super(id, executor);
    }

    public PowerShellVmPoolResource(String id) {
        super(id);
    }

    public static ArrayList<VmPool> runAndParse(String command) {
        return PowerShellVmPool.parse(PowerShellCmd.runCommand(command));
    }

    public static VmPool runAndParseSingle(String command) {
        ArrayList<VmPool> pools = runAndParse(command);

        return !pools.isEmpty() ? pools.get(0) : null;
    }

    /* Map the template name to template ID on a VM pool, since
     * the powershell output only includes the template name.
     *
     * @param pool  the VM pool to modify
     * @return      the modified VM pool
     */
    private static VmPool lookupTemplateId(VmPool pool) {
        StringBuilder buf = new StringBuilder();

        buf.append("select-template -searchtext 'name = " + pool.getTemplate().getName() + "'");

        Template template = new Template();
        template.setId(PowerShellTemplateResource.runAndParseSingle(buf.toString()).getId());
        pool.setTemplate(template);

        return pool;
    }

    /* Map the cluster name to cluster ID on a VM pool, since
     * the powershell output only includes the cluster name.
     *
     * @param pool  the VM pool to modify
     * @return      the modified VM pool
     */
    private static VmPool lookupClusterId(VmPool pool) {
        StringBuilder buf = new StringBuilder();

        buf.append("select-cluster -searchtext 'name = " + pool.getCluster().getName() + "'");

        Cluster cluster = new Cluster();
        cluster.setId(PowerShellClusterResource.runAndParseSingle(buf.toString()).getId());
        pool.setCluster(cluster);

        return pool;
    }

    public static VmPool addLinks(VmPool pool, UriInfo uriInfo, UriBuilder uriBuilder) {
        pool.setHref(uriBuilder.build().toString());

        UriBuilder baseUriBuilder = uriInfo.getBaseUriBuilder();

        pool = lookupClusterId(pool);
        Cluster cluster = pool.getCluster();
        cluster.setHref(PowerShellClustersResource.getHref(baseUriBuilder, cluster.getId()));

        pool = lookupTemplateId(pool);
        Template template = pool.getTemplate();
        template.setHref(PowerShellTemplatesResource.getHref(baseUriBuilder, template.getId()));

        return pool;
    }

    @Override
    public VmPool get(UriInfo uriInfo) {
        return addLinks(runAndParseSingle("get-vmpool -vmpoolid " + getId()), uriInfo, uriInfo.getRequestUriBuilder());
    }

    @Override
    public VmPool update(HttpHeaders headers, UriInfo uriInfo, VmPool pool) {
        validateUpdate(pool, headers);

        StringBuilder buf = new StringBuilder();

        buf.append("$v = get-vmpool " + getId() + "\n");

        if (pool.getName() != null) {
            buf.append("$v.name = '" + pool.getName() + "'\n");
        }
        if (pool.getDescription() != null) {
            buf.append("$v.description = '" + pool.getDescription() + "'\n");
        }
        if (pool.getSize() != null) {
            buf.append("$v.vmcount = " + pool.getSize() + "\n");
        }

        buf.append("update-vmpool -vmpoolobject $v");

        return addLinks(runAndParseSingle(buf.toString()), uriInfo, uriInfo.getRequestUriBuilder());
    }
}

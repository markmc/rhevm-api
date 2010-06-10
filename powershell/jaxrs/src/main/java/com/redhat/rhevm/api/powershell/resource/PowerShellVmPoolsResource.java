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

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.VmPool;
import com.redhat.rhevm.api.model.VmPools;
import com.redhat.rhevm.api.resource.VmPoolResource;
import com.redhat.rhevm.api.resource.VmPoolsResource;
import com.redhat.rhevm.api.powershell.model.PowerShellVmPool;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;


public class PowerShellVmPoolsResource
    extends AbstractPowerShellCollectionResource<VmPool, PowerShellVmPoolResource>
    implements VmPoolsResource {

    @Override
    public VmPools list(UriInfo uriInfo) {
        VmPools ret = new VmPools();
        for (VmPool pool : PowerShellVmPoolResource.runAndParse(getSelectCommand("select-vmpool", uriInfo, VmPools.class))) {
            UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(pool.getId());
            ret.getVmPools().add(PowerShellVmPoolResource.addLinks(pool, uriInfo, uriBuilder));
        }
        return ret;
    }

    @Override
    public Response add(UriInfo uriInfo, VmPool pool) {
        StringBuilder buf = new StringBuilder();

        buf.append("add-vmpool");

        buf.append(" -vmpoolname '" + pool.getName() + "'");
        buf.append(" -templateid " + pool.getTemplate().getId());
        buf.append(" -hostclusterid " + pool.getCluster().getId());
        buf.append(" -pooltype Automatic");

        if (pool.getDescription() != null) {
            buf.append(" -vmpooldescription '" + pool.getDescription() + "'");
        }
        if (pool.getSize() != null) {
            buf.append(" -numofvms " + pool.getSize());
        }

        pool = PowerShellVmPoolResource.runAndParseSingle(buf.toString());

        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(pool.getId());

        pool = PowerShellVmPoolResource.addLinks(pool, uriInfo, uriBuilder);

        return Response.created(uriBuilder.build()).entity(pool).build();
    }

    @Override
    public void remove(String id) {
        StringBuilder buf = new StringBuilder();

        buf.append("$p = get-vmpool -vmpoolid " + id + "\n");
        buf.append("remove-vmpool -name $p.name");

        PowerShellCmd.runCommand(buf.toString());

        removeSubResource(id);
    }

    @Override
    public VmPoolResource getVmPoolSubResource(UriInfo uriInfo, String id) {
        return getSubResource(id);
    }

    @Override
    protected PowerShellVmPoolResource createSubResource(String id) {
        return new PowerShellVmPoolResource(id, getExecutor());
    }

    /**
     * Build an absolute URI for a given VM pool
     *
     * @param baseUriBuilder a UriBuilder representing the base URI
     * @param id             the VM pool ID
     * @return               an absolute URI
     */
    public static String getHref(UriBuilder baseUriBuilder, String id) {
        return baseUriBuilder.clone().path("vmpools").path(id).build().toString();
    }
}

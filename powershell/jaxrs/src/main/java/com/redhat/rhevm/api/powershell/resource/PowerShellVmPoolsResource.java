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

import java.util.List;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.VmPool;
import com.redhat.rhevm.api.model.VmPools;
import com.redhat.rhevm.api.resource.VmPoolResource;
import com.redhat.rhevm.api.resource.VmPoolsResource;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;

import static com.redhat.rhevm.api.common.util.CompletenessAssertor.validateParameters;


public class PowerShellVmPoolsResource
    extends AbstractPowerShellCollectionResource<VmPool, PowerShellVmPoolResource>
    implements VmPoolsResource {

    public List<VmPool> runAndParse(String command) {
        return PowerShellVmPoolResource.runAndParse(getPool(), getParser(), command);
    }

    public VmPool runAndParseSingle(String command) {
        return PowerShellVmPoolResource.runAndParseSingle(getPool(), getParser(), command);
    }

    public VmPool addLinks(VmPool pool) {
        return PowerShellVmPoolResource.addLinks(getPool(), getParser(), pool);
    }

    @Override
    public VmPools list(UriInfo uriInfo) {
        VmPools ret = new VmPools();
        for (VmPool pool : runAndParse(getSelectCommand("select-vmpool", uriInfo, VmPool.class))) {
            ret.getVmPools().add(addLinks(pool));
        }
        return ret;
    }

    @Override
    public Response add(UriInfo uriInfo, VmPool pool) {
        validateParameters(pool, "name", "template.id|name", "cluster.id|name");
        StringBuilder buf = new StringBuilder();

        String templateArg = null;
        if (pool.getTemplate().isSetId()) {
            templateArg = PowerShellUtils.escape(pool.getTemplate().getId());
        } else {
            buf.append("$t = select-template -searchtext ");
            buf.append(PowerShellUtils.escape("name=" + pool.getTemplate().getName()));
            buf.append(";");
            templateArg = "$t.TemplateId";
        }

        String clusterArg = null;
        if (pool.getCluster().isSetId()) {
            clusterArg = PowerShellUtils.escape(pool.getCluster().getId());
        } else {
            buf.append("$c = select-cluster -searchtext ");
            buf.append(PowerShellUtils.escape("name=" +  pool.getCluster().getName()));
            buf.append(";");
            clusterArg = "$c.ClusterId";
        }

        buf.append("add-vmpool");

        buf.append(" -vmpoolname " + PowerShellUtils.escape(pool.getName()));
        if (pool.getDescription() != null) {
            buf.append(" -vmpooldescription " + PowerShellUtils.escape(pool.getDescription()));
        }
        buf.append(" -templateid " + templateArg);
        buf.append(" -hostclusterid " + clusterArg);
        buf.append(" -pooltype Automatic");
        if (pool.getSize() != null) {
            buf.append(" -numofvms " + pool.getSize());
        }

        pool = addLinks(runAndParseSingle(buf.toString()));

        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(pool.getId());

        return Response.created(uriBuilder.build()).entity(pool).build();
    }

    @Override
    public void remove(String id) {
        StringBuilder buf = new StringBuilder();

        buf.append("$p = get-vmpool -vmpoolid " + PowerShellUtils.escape(id) + ";");
        buf.append("remove-vmpool -name $p.name");

        PowerShellCmd.runCommand(getPool(), buf.toString());

        removeSubResource(id);
    }

    @Override
    public VmPoolResource getVmPoolSubResource(UriInfo uriInfo, String id) {
        return getSubResource(id);
    }

    @Override
    protected PowerShellVmPoolResource createSubResource(String id) {
        return new PowerShellVmPoolResource(id, getExecutor(), shellPools, getParser());
    }
}

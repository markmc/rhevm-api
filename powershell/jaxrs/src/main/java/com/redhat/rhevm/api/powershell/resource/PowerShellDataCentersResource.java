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

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.DataCenter;
import com.redhat.rhevm.api.model.DataCenters;
import com.redhat.rhevm.api.resource.DataCenterResource;
import com.redhat.rhevm.api.resource.DataCentersResource;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;

public class PowerShellDataCentersResource implements DataCentersResource {
    /* FIXME: would like to do:
     * private @Context UriInfo uriInfo;
     */

    private DataCenters addLinks(List<DataCenter> datacenters, UriInfo uriInfo) {
        DataCenters ret = new DataCenters();
        for (DataCenter datacenter : datacenters) {
            UriBuilder uriBuilder = uriInfo.getRequestUriBuilder().path(datacenter.getId());
            ret.getDataCenters().add(PowerShellDataCenterResource.addLink(datacenter, uriBuilder.build()));
        }
        return ret;
    }

    @Override
    public DataCenters list(UriInfo uriInfo) {
        return addLinks(PowerShellDataCenterResource.runAndParse("select-datacenter"), uriInfo);
    }

/* FIXME: move this
   @Override
   public VMs search(String criteria) {
   return runAndParse("select-datacenter " + criteria);
   }
*/

    @Override
    public Response add(UriInfo uriInfo, DataCenter datacenter) {
        StringBuilder buf = new StringBuilder();

        buf.append("add-datacenter");

        buf.append(" -type " + datacenter.getStorageType().toString());

        if (datacenter.getName() != null) {
            buf.append(" -name " + datacenter.getName());
        }

        datacenter = PowerShellDataCenterResource.runAndParseSingle(buf.toString());

        URI uri = uriInfo.getRequestUriBuilder().path(datacenter.getId()).build();

        return Response.created(uri).entity(PowerShellDataCenterResource.addLink(datacenter, uri)).build();
    }

    @Override
    public void remove(String id) {
        PowerShellUtils.runCommand("remove-datacenter -datacenterid " + id);
    }

    @Override
    public DataCenterResource getDataCenterSubResource(UriInfo uriInfo, String id) {
        return new PowerShellDataCenterResource(id);
    }
}

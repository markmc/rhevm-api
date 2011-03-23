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

import com.redhat.rhevm.api.model.DataCenter;
import com.redhat.rhevm.api.model.DataCenters;
import com.redhat.rhevm.api.model.StorageType;
import com.redhat.rhevm.api.model.Version;
import com.redhat.rhevm.api.resource.DataCenterResource;
import com.redhat.rhevm.api.resource.DataCentersResource;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;

import static com.redhat.rhevm.api.common.util.CompletenessAssertor.validateParameters;
import static com.redhat.rhevm.api.common.util.EnumValidator.validateEnum;

public class PowerShellDataCentersResource
    extends AbstractPowerShellCollectionResource<DataCenter, PowerShellDataCenterResource>
    implements DataCentersResource {

    public List<DataCenter> runAndParse(String command) {
        return PowerShellDataCenterResource.runAndParse(getPool(), getParser(), command);
    }

    public DataCenter runAndParseSingle(String command) {
        return PowerShellDataCenterResource.runAndParseSingle(getPool(), getParser(), command);
    }

    public DataCenter addLinks(DataCenter dataCenter) {
        return PowerShellDataCenterResource.addLinks(getUriInfo(), getPool(), getParser(), dataCenter);
    }

    @Override
    public DataCenters list() {
        DataCenters ret = new DataCenters();
        for (DataCenter dataCenter : runAndParse(getSelectCommand("select-datacenter", getUriInfo(), DataCenter.class))) {
            ret.getDataCenters().add(addLinks(dataCenter));
        }
        return ret;
    }

    @Override
    public Response add(DataCenter dataCenter) {
        validateParameters(dataCenter, "name", "storageType");

        StorageType storageType = validateEnum(StorageType.class, dataCenter.getStorageType().toUpperCase());

        StringBuilder buf = new StringBuilder();

        buf.append("foreach ($v in get-clustercompatibilityversions) { ");
        if (dataCenter.isSetVersion() &&
            dataCenter.getVersion().isSetMajor() &&
            dataCenter.getVersion().isSetMinor()) {
            Version v = dataCenter.getVersion();
            buf.append("if (");
            buf.append("$v.major -eq " + Integer.toString(v.getMajor()));
            buf.append(" -and ");
            buf.append("$v.minor -eq " + Integer.toString(v.getMinor()));
            buf.append(") { ");
            buf.append("$version = $v; break");
            buf.append(" }");
        } else {
            buf.append("$version = $v");
        }
        buf.append(" } ");

        buf.append("add-datacenter");

        buf.append(" -name " + PowerShellUtils.escape(dataCenter.getName()));
        if (dataCenter.getDescription() != null) {
            buf.append(" -description " + PowerShellUtils.escape(dataCenter.getDescription()));
        }
        buf.append(" -datacentertype " + storageType.name());
        buf.append(" -compatibilityversion $version");

        dataCenter = addLinks(runAndParseSingle(buf.toString()));

        UriBuilder uriBuilder = getUriInfo().getAbsolutePathBuilder().path(dataCenter.getId());

        return Response.created(uriBuilder.build()).entity(dataCenter).build();
    }

    @Override
    public void remove(String id) {
        PowerShellCmd.runCommand(getPool(), "remove-datacenter -datacenterid " + PowerShellUtils.escape(id));
        removeSubResource(id);
    }

    @Override
    public DataCenterResource getDataCenterSubResource(String id) {
        return getSubResource(id);
    }

    protected PowerShellDataCenterResource createSubResource(String id) {
        return new PowerShellDataCenterResource(id, getExecutor(), this, shellPools, getParser());
    }
}

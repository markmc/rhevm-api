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
import java.util.concurrent.Executor;

import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.DataCenter;
import com.redhat.rhevm.api.model.Link;
import com.redhat.rhevm.api.model.Version;
import com.redhat.rhevm.api.model.SupportedVersions;
import com.redhat.rhevm.api.resource.AttachedStorageDomainsResource;
import com.redhat.rhevm.api.resource.DataCenterResource;
import com.redhat.rhevm.api.resource.IsosResource;
import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.powershell.model.PowerShellDataCenter;
import com.redhat.rhevm.api.powershell.model.PowerShellVersion;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPool;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;


public class PowerShellDataCenterResource extends AbstractPowerShellActionableResource<DataCenter> implements DataCenterResource {

    public PowerShellDataCenterResource(String id,
                                        Executor executor,
                                        UriInfoProvider uriProvider,
                                        PowerShellPoolMap shellPools,
                                        PowerShellParser parser) {
        super(id, executor, uriProvider, shellPools, parser);
    }

    public static List<DataCenter> runAndParse(PowerShellPool pool, PowerShellParser parser, String command) {
        return PowerShellDataCenter.parse(parser, PowerShellCmd.runCommand(pool, command));
    }

    public static DataCenter runAndParseSingle(PowerShellPool pool, PowerShellParser parser, String command) {
        List<DataCenter> dataCenters = runAndParse(pool, parser, command);

        return !dataCenters.isEmpty() ? dataCenters.get(0) : null;
    }

    public List<DataCenter> runAndParse(String command) {
        return runAndParse(getPool(), getParser(), command);
    }

    public DataCenter runAndParseSingle(String command) {
        return runAndParseSingle(getPool(), getParser(), command);
    }

    private static DataCenter querySupportedVersions(PowerShellPool pool, PowerShellParser parser, DataCenter dataCenter) {
        String command = "get-datacentercompatibilityversions -datacenterid " + PowerShellUtils.escape(dataCenter.getId());

        List<Version> supported = PowerShellVersion.parse(parser, PowerShellCmd.runCommand(pool, command));
        if (!supported.isEmpty()) {
            dataCenter.setSupportedVersions(new SupportedVersions());
            for (Version v : supported) {
                dataCenter.getSupportedVersions().getVersions().add(v);
            }
        }

        return dataCenter;
    }

    public static DataCenter addLinks(UriInfo uriInfo, PowerShellPool pool, PowerShellParser parser, DataCenter dataCenter) {
        dataCenter = querySupportedVersions(pool, parser, dataCenter);

        String [] subCollections = { "isos", "storagedomains" };

        dataCenter.getLinks().clear();

        for (String collection : subCollections) {
            addSubCollection(uriInfo, dataCenter, collection);
        }

        return LinkHelper.addLinks(uriInfo, dataCenter);
    }

    private static void addSubCollection(UriInfo uriInfo, DataCenter dataCenter, String collection) {
        Link link = new Link();
        link.setRel(collection);
        link.setHref(LinkHelper.getUriBuilder(uriInfo, dataCenter).path(collection).build().toString());
        dataCenter.getLinks().add(link);
    }

    public DataCenter addLinks(DataCenter dataCenter) {
        return addLinks(getUriInfo(), getPool(), getParser(), dataCenter);
    }

    @Override
    public DataCenter get() {
        return addLinks(runAndParseSingle("get-datacenter " + PowerShellUtils.escape(getId())));
    }

    @Override
    public DataCenter update(DataCenter dataCenter) {
        validateUpdate(dataCenter);

        StringBuilder buf = new StringBuilder();

        buf.append("$d = get-datacenter " + PowerShellUtils.escape(getId()) + "; ");

        if (dataCenter.getName() != null) {
            buf.append("$d.name = " + PowerShellUtils.escape(dataCenter.getName()) + ";");
        }
        if (dataCenter.getDescription() != null) {
            buf.append("$d.description = " + PowerShellUtils.escape(dataCenter.getDescription()) + "; ");
        }

        if (dataCenter.isSetVersion() &&
            dataCenter.getVersion().isSetMajor() &&
            dataCenter.getVersion().isSetMinor()) {
            Version v = dataCenter.getVersion();
            buf.append("foreach ($v in get-clustercompatibilityversions) { ");
            buf.append("if (");
            buf.append("$v.major -eq " + Integer.toString(v.getMajor()));
            buf.append(" -and ");
            buf.append("$v.minor -eq " + Integer.toString(v.getMinor()));
            buf.append(") { ");
            buf.append("$d.compatibilityversion = $v; break");
            buf.append(" } } ");
        }

        buf.append("update-datacenter -datacenterobject $d");

        return addLinks(runAndParseSingle(buf.toString()));
    }

    public IsosResource getIsosResource() {
        return new PowerShellIsosResource(getId(), shellPools, getParser(), uriProvider);
    }

    public AttachedStorageDomainsResource getAttachedStorageDomainsResource() {
        PowerShellAttachedStorageDomainsResource resource =
            new PowerShellAttachedStorageDomainsResource(getId(), shellPools, getParser());
        resource.setUriInfo(getUriInfo());
        return resource;
    }
}

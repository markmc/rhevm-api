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

import java.util.concurrent.Executor;

import javax.ws.rs.core.Response;

import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.BaseResource;
import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;

import static com.redhat.rhevm.api.common.util.CompletenessAssertor.validateParameters;

public abstract class AbstractPowerShellStorageDomainContentResource<R extends BaseResource>
    extends AbstractPowerShellActionableResource<R> {

    protected AbstractPowerShellStorageDomainContentsResource<R> parent;

    public AbstractPowerShellStorageDomainContentResource(AbstractPowerShellStorageDomainContentsResource<R> parent,
                                                          String id,
                                                          Executor executor,
                                                          UriInfoProvider uriProvider,
                                                          PowerShellPoolMap shellPools,
                                                          PowerShellParser parser) {
        super(id, executor, uriProvider, shellPools, parser);
        this.parent = parent;
    }

    public String getStorageDomainId() {
        return parent.getStorageDomainId();
    }

    protected Response doImport(Action action, String type) {
        validateParameters(action, "cluster.id|name", "storageDomain.id|name");

        StringBuilder buf = new StringBuilder();

        String clusterArg = getClusterArg(buf, action.getCluster());
        String destDomainArg = getDestDomainArg(buf, action.getStorageDomain());

        buf.append("import-" + type);
        // buf.append(" -datacenterid " + PowerShellUtils.escape(getDataCenterId()));
        buf.append(" -sourcedomainid " + PowerShellUtils.escape(getStorageDomainId()));
        buf.append(" -destdomainid " + destDomainArg);
        buf.append(" -clusterid " + clusterArg);
        buf.append(" -" + type + "id " + PowerShellUtils.escape(getId()));

        return doAction(getUriInfo(), new CommandRunner(action, buf.toString(), getPool()));
    }

    protected String getClusterArg(StringBuilder buf, Cluster cluster) {
        String ret;
        if (cluster.isSetId()) {
            ret = PowerShellUtils.escape(cluster.getId());
        } else {
            buf.append("$c = select-cluster -searchtext ");
            buf.append(PowerShellUtils.escape("name=" + cluster.getName()));
            buf.append("; ");
            ret = "$c.clusterid";
        }
        return ret;
    }

    protected String getDestDomainArg(StringBuilder buf, StorageDomain destDomain) {
        String ret;
        if (destDomain.isSetId()) {
            ret = PowerShellUtils.escape(destDomain.getId());
        } else {
            buf.append("$d = select-storagedomain -searchtext ");
            buf.append(PowerShellUtils.escape("name=" + destDomain.getName()));
            buf.append("; ");
            ret = "$d.storagedomainid";
        }
        return ret;
    }
}

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
import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.model.HostNIC;
import com.redhat.rhevm.api.resource.HostNicResource;
import com.redhat.rhevm.api.resource.StatisticsResource;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;

import static com.redhat.rhevm.api.common.util.CompletenessAssertor.validateParameters;

public class PowerShellHostNicResource
    extends AbstractPowerShellActionableResource<HostNIC>
    implements HostNicResource {

    private PowerShellHostNicsResource parent;

    public PowerShellHostNicResource(String id,
                                     Executor executor,
                                     PowerShellPoolMap shellPools,
                                     PowerShellParser parser,
                                     PowerShellHostNicsResource parent,
                                     UriInfoProvider uriProvider) {
        super(id, executor, uriProvider, shellPools);
        this.parent = parent;
    }

    @Override
    public HostNIC get() {
        return parent.addLinks(parent.getHostNic(getId()));
    }

    @Override
    protected HostNIC getModel() {
        HostNIC actionParent = super.getModel();
        actionParent.setHost(new Host());
        actionParent.getHost().setId(parent.getHostId());
        return actionParent;
    }

    private Response doNetworkAdapterAction(Action action, String command) {
        validateParameters(action, "network.id|name");

        StringBuilder buf = new StringBuilder();

        buf.append("$h = get-host " + PowerShellUtils.escape(parent.getHostId()) + "; ");

        buf.append("foreach ($n in get-networks) { ");
        if (action.getNetwork().isSetId()) {
            buf.append("if ($n.networkid -eq " + PowerShellUtils.escape(action.getNetwork().getId()) + ") { ");
        } else {
            buf.append("if ($n.name -eq " + PowerShellUtils.escape(action.getNetwork().getName()) + ") { ");
        }
        buf.append("$net = $n; break ");
        buf.append("} ");
        buf.append("} ");

        buf.append("foreach ($n in $h.getnetworkadapters()) { ");
        buf.append("if ($n.id -eq " + PowerShellUtils.escape(getId()) + ") { ");
        buf.append("$nic = $n; break ");
        buf.append("} ");
        buf.append("} ");

        buf.append(command);
        buf.append(" -hostobject $h");
        buf.append(" -network $net");
        buf.append(" -networkadapter $nic");

        return doAction(getUriInfo(), new CommandRunner(action, buf.toString(), getPool()));
    }

    @Override
    public Response attach(Action action) {
        return doNetworkAdapterAction(action, "attach-logicalnetworktonetworkadapter");
    }

    @Override
    public Response detach(Action action) {
        return doNetworkAdapterAction(action, "detach-logicalnetworkfromnetworkadapter");
    }

    @Override
    public StatisticsResource getStatisticsResource() {
        // REVISIT
        return null;
    }
}

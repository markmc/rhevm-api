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
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.Snapshot;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.resource.SnapshotResource;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;

public class PowerShellSnapshotResource
    extends AbstractPowerShellActionableResource<Snapshot>
    implements SnapshotResource {

    private PowerShellSnapshotsResource parent;

    public PowerShellSnapshotResource(String id,
                                      Executor executor,
                                      PowerShellPoolMap shellPools,
                                      PowerShellParser parser,
                                      PowerShellSnapshotsResource parent) {
        super(id, executor, shellPools, parser);
        this.parent = parent;
    }

    protected Snapshot getModel() {
        Snapshot actionParent = super.getModel();
        actionParent.setVm(new VM());
        actionParent.getVm().setId(parent.getVmId());
        return actionParent;
    }

    @Override
    public Snapshot get() {
        return parent.buildFromDisk(parent.getDiskSnapshot(getId()));
    }

    @Override
    public Response restore(UriInfo uriInfo, Action action) {
        StringBuilder buf = new StringBuilder();

        buf.append("restore-vm");
        buf.append(" -vmid " + PowerShellUtils.escape(parent.getVmId()));
        buf.append(" -vmsnapshotid " + PowerShellUtils.escape(getId()));

        return doAction(uriInfo, new CommandRunner(action, buf.toString(), getPool()));
    }
}

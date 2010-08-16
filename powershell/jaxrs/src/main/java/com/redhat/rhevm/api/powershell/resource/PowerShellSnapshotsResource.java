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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Produces;
import javax.ws.rs.core.UriBuilder;

import com.redhat.rhevm.api.resource.MediaType;

import com.redhat.rhevm.api.model.Link;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.model.Snapshot;
import com.redhat.rhevm.api.model.Snapshots;
import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.powershell.model.PowerShellDisk;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
import com.redhat.rhevm.api.powershell.util.UUID;
import com.redhat.rhevm.api.resource.SnapshotResource;
import com.redhat.rhevm.api.resource.SnapshotsResource;

@Produces(MediaType.APPLICATION_XML)
public class PowerShellSnapshotsResource implements SnapshotsResource {

    protected String vmId;
    protected PowerShellPoolMap shellPools;
    protected PowerShellParser parser;

    public PowerShellSnapshotsResource(String vmId,
                                       PowerShellPoolMap shellPools,
                                       PowerShellParser parser) {
        this.vmId = vmId;
        this.shellPools = shellPools;
        this.parser = parser;
    }

    public PowerShellCmd getShell() {
        return shellPools.get().get();
    }

    public PowerShellParser getParser() {
        return parser;
    }

    public List<PowerShellDisk> runAndParse(String command) {
        return PowerShellDisk.parse(getParser(), vmId, PowerShellCmd.runCommand(getShell(), command));
    }

    public PowerShellDisk runAndParseSingle(String command) {
        List<PowerShellDisk> disks = runAndParse(command);
        return !disks.isEmpty() ? disks.get(0) : null;
    }

    public Snapshot addLinks(Snapshot snapshot) {
        return LinkHelper.addLinks(snapshot);
    }

    private List<PowerShellDisk> getDiskSnapshots() {
        StringBuilder buf = new StringBuilder();

        buf.append("$snaps = @(); ");
        buf.append("$vm = get-vm " + PowerShellUtils.escape(vmId) + "; ");
        buf.append("foreach ($d in $vm.getdiskimages()) { ");
        buf.append("$snaps += get-snapshot -vmid $vm.vmid -drive $d.internaldrivemapping ");
        buf.append("} ");
        buf.append("$snaps");

        return runAndParse(buf.toString());
    }

    @Override
    public Snapshots list() {
        Map<String, Snapshot> snapshots = new HashMap<String, Snapshot>();

        VM vm = new VM();
        vm.setId(vmId);

        UriBuilder uriBuilder = LinkHelper.getUriBuilder(vm).path("snapshots");

        for (PowerShellDisk disk : getDiskSnapshots()) {
            if (snapshots.containsKey(disk.getVmSnapshotId())) {
                continue;
            }

            Snapshot snapshot = new Snapshot();
            snapshot.setVm(vm);
            snapshot.setId(disk.getVmSnapshotId());
            snapshot.setDescription(disk.getDescription());
            snapshot.setDate(disk.getLastModified());

            if (!disk.getParentId().equals(UUID.EMPTY)) {
                Link prev = new Link();
                prev.setRel("prev");
                prev.setHref(uriBuilder.clone().path(disk.getParentId()).build().toString());
                snapshot.getLinks().add(prev);
            }

            snapshots.put(snapshot.getId(), snapshot);
        }

        Snapshots ret = new Snapshots();
        for (String id : sortedKeys(snapshots)) {
            ret.getSnapshots().add(addLinks(snapshots.get(id)));
        }
        return ret;
    }

    @Override
    public SnapshotResource getSnapshotSubResource(String id) {
        return null;
    }

    private List<String> sortedKeys(Map<String, Snapshot> map) {
        List<String> keys = new ArrayList(map.keySet());
        Collections.sort(keys);
        return keys;
    }
}

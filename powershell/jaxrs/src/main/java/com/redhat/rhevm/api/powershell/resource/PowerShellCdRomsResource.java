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

import com.redhat.rhevm.api.model.CdRom;
import com.redhat.rhevm.api.model.CdRoms;
import com.redhat.rhevm.api.model.Iso;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.powershell.model.PowerShellVM;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;


public class PowerShellCdRomsResource extends AbstractPowerShellDevicesResource<CdRom, CdRoms> {

    private static final String CDROM_ID = Integer.toString("cdrom".hashCode());

    public PowerShellCdRomsResource(String vmId) {
        super(vmId);
    }

    private CdRom buildCdRom(String cdIsoPath) {
        CdRom cdrom = new CdRom();
        cdrom.setId(CDROM_ID);
        cdrom.setIso(new Iso());
        cdrom.getIso().setId(cdIsoPath);
        cdrom.setVm(new VM());
        cdrom.getVm().setId(vmId);
        return cdrom;
    }

    @Override
    public CdRoms getDevices() {
        CdRoms cdroms = new CdRoms();

        PowerShellVM vm = PowerShellVmResource.runAndParseSingle("get-vm " + PowerShellUtils.escape(vmId));

        if (vm.getCdIsoPath() != null) {
            cdroms.getCdRoms().add(buildCdRom(vm.getCdIsoPath()));
        }

        return cdroms;
    }

    @Override
    public CdRom addLinks(CdRom cdrom) {
        return LinkHelper.addLinks(cdrom);
    }

    @Override
    public CdRoms list() {
        CdRoms cdroms = getDevices();
        for (CdRom cdrom : cdroms.getCdRoms()) {
            addLinks(cdrom);
        }
        return cdroms;
    }

    private void updateCdRom(String cdIsoPath) {
        StringBuilder buf = new StringBuilder();

        buf.append("$v = get-vm " + PowerShellUtils.escape(vmId) + "\n");
        buf.append("$v.cdisopath = " + PowerShellUtils.escape(cdIsoPath) + "\n");
        buf.append("update-vm -vmobject $v");

        PowerShellCmd.runCommand(buf.toString());
    }

    @Override
    public Response add(UriInfo uriInfo, CdRom cdrom) {
        String cdIsoPath = cdrom.getIso().getId();

        updateCdRom(cdIsoPath);

        cdrom = addLinks(buildCdRom(cdIsoPath));

        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(cdrom.getId());

        return Response.created(uriBuilder.build()).entity(cdrom).build();
    }

    @Override
    public void remove(String id) {
        if (id.equals(CDROM_ID)) {
            updateCdRom("");
        }
    }

    @Override
    public PowerShellDeviceResource<CdRom, CdRoms> getDeviceSubResource(String id) {
        return new PowerShellDeviceResource<CdRom, CdRoms>(this, id);
    }
}

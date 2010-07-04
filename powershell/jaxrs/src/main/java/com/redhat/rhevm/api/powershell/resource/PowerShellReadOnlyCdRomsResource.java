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

import com.redhat.rhevm.api.model.CdRom;
import com.redhat.rhevm.api.model.CdRoms;
import com.redhat.rhevm.api.model.Iso;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.powershell.model.PowerShellVM;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;


public class PowerShellReadOnlyCdRomsResource extends AbstractPowerShellDevicesResource<CdRom, CdRoms> {

    private static final String CDROM_ID = Integer.toString("cdrom".hashCode());

    private CdRomQuery query;

    public PowerShellReadOnlyCdRomsResource(String parentId, PowerShellPoolMap shellPools, CdRomQuery query) {
        super(parentId, shellPools);
        this.query = query;
    }

    protected CdRom buildCdRom(String cdIsoPath) {
        CdRom cdrom = new CdRom();
        cdrom.setId(CDROM_ID);
        cdrom.setIso(new Iso());
        cdrom.getIso().setId(cdIsoPath);
        cdrom.setVm(new VM());
        cdrom.getVm().setId(parentId);
        return cdrom;
    }

    @Override
    public CdRoms getDevices() {
        CdRoms cdroms = new CdRoms();

        String cdIsoPath = query.getCdIsoPath(getShell());
        if (cdIsoPath != null) {
            cdroms.getCdRoms().add(buildCdRom(cdIsoPath));
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

    @Override
    public PowerShellDeviceResource<CdRom, CdRoms> getDeviceSubResource(String id) {
        return new PowerShellDeviceResource<CdRom, CdRoms>(this, id, shellPools);
    }

    public static abstract class CdRomQuery {
        protected String id;
        public CdRomQuery(String id) {
            this.id = id;
        }
        protected abstract String getCdIsoPath(PowerShellCmd shell);
    }
}

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

import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.model.CdRom;
import com.redhat.rhevm.api.model.CdRoms;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
import com.redhat.rhevm.api.resource.DevicesResource;

import static com.redhat.rhevm.api.common.util.CompletenessAssertor.validateParameters;


public class PowerShellCdRomsResource
    extends PowerShellReadOnlyCdRomsResource
    implements DevicesResource<CdRom, CdRoms> {

    private static final String CDROM_ID = Integer.toString("cdrom".hashCode());

    public PowerShellCdRomsResource(String parentId, PowerShellPoolMap shellPools, CdRomQuery query, UriInfoProvider uriProvider) {
        super(parentId, shellPools, query, uriProvider);
    }

    private void updateCdRom(String cdIsoPath) {
        StringBuilder buf = new StringBuilder();

        buf.append("$v = get-vm " + PowerShellUtils.escape(parentId) + ";");
        buf.append("$v.cdisopath = " + PowerShellUtils.escape(cdIsoPath) + ";");
        buf.append("update-vm -vmobject $v");

        PowerShellCmd.runCommand(getPool(), buf.toString());
    }

    @Override
    public Response add(CdRom cdrom) {
        validateParameters(cdrom, "file");
        String cdIsoPath = cdrom.getFile().getId();

        updateCdRom(cdIsoPath);

        cdrom = addLinks(buildCdRom(cdIsoPath));

        UriBuilder uriBuilder = getUriInfo().getAbsolutePathBuilder().path(cdrom.getId());

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

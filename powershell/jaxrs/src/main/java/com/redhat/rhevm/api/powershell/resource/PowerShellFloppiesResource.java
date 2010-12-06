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
import com.redhat.rhevm.api.model.Floppy;
import com.redhat.rhevm.api.model.Floppies;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
import com.redhat.rhevm.api.resource.DevicesResource;

import static com.redhat.rhevm.api.common.util.CompletenessAssertor.validateParameters;

public class PowerShellFloppiesResource
    extends PowerShellReadOnlyFloppiesResource
    implements DevicesResource<Floppy, Floppies> {

    private static final String FLOPPY_ID = Integer.toString("floppy".hashCode());

    public PowerShellFloppiesResource(String parentId,
                                      PowerShellPoolMap shellPools,
                                      FloppyQuery query,
                                      UriInfoProvider uriProvider) {
        super(parentId, shellPools, query, uriProvider);
    }

    Floppy updateFloppy(String floppyPath) {
        StringBuilder buf = new StringBuilder();

        buf.append("$v = get-vm " + PowerShellUtils.escape(parentId) + ";");
        buf.append("$v.floppypath = " + PowerShellUtils.escape(floppyPath) + ";");
        buf.append("update-vm -vmobject $v");

        PowerShellCmd.runCommand(getPool(), buf.toString());
        return buildFloppy(floppyPath);
    }

    @Override
    public Response add(Floppy floppy) {
        validateParameters(floppy, "file");
        String floppyPath = floppy.getFile().getId();

        updateFloppy(floppyPath);

        floppy = buildFloppy(floppyPath);

        UriBuilder uriBuilder = getUriInfo().getAbsolutePathBuilder().path(floppy.getId());

        return Response.created(uriBuilder.build()).entity(floppy).build();
    }

    @Override
    public void remove(String id) {
        if (id.equals(FLOPPY_ID)) {
            updateFloppy("");
        }
    }

    @Override
    public PowerShellDeviceResource<Floppy, Floppies> getDeviceSubResource(String id) {
        return new PowerShellFloppyDeviceResource(this, id);
    }
}

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
import java.util.List;

import com.redhat.rhevm.api.model.Floppy;
import com.redhat.rhevm.api.model.Floppies;
import com.redhat.rhevm.api.model.File;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;

public class PowerShellReadOnlyFloppiesResource extends AbstractPowerShellDevicesResource<Floppy, Floppies> {

    private static final String FLOPPY_ID = Integer.toString("floppy".hashCode());

    private FloppyQuery query;

    public PowerShellReadOnlyFloppiesResource(String parentId,
                                              PowerShellPoolMap shellPools,
                                              FloppyQuery query,
                                              UriInfoProvider uriProvider) {
        super(parentId, shellPools, uriProvider);
        this.query = query;
    }

    protected Floppy buildFloppy(String floppyPath) {
        Floppy floppy = new Floppy();
        floppy.setId(FLOPPY_ID);
        floppy.setFile(new File());
        floppy.getFile().setId(floppyPath);
        floppy.setVm(new VM());
        floppy.getVm().setId(parentId);
        return addLinks(floppy);
    }

    @Override
    public List<Floppy> getDevices() {
        List<Floppy> floppies = new ArrayList<Floppy>();

        String floppyPath = query.getFloppyPath();
        if (floppyPath != null) {
            floppies.add(buildFloppy(floppyPath));
        }

        return floppies;
    }

    @Override
    public Floppy addLinks(Floppy floppy) {
        return LinkHelper.addLinks(getUriInfo(), floppy);
    }

    @Override
    public Floppies list() {
        Floppies floppies = new Floppies();
        for (Floppy floppy : getDevices()) {
            floppies.getFloppies().add(addLinks(floppy));
        }
        return floppies;
    }

    @Override
    public PowerShellReadOnlyDeviceResource<Floppy, Floppies> getDeviceSubResource(String id) {
        return new PowerShellReadOnlyDeviceResource<Floppy, Floppies>(this, id);
    }

    public static abstract class FloppyQuery {
        protected String id;
        public FloppyQuery(String id) {
            this.id = id;
        }
        protected abstract String getFloppyPath();
    }
}

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

import com.redhat.rhevm.api.model.BaseDevice;
import com.redhat.rhevm.api.model.BaseDevices;
import com.redhat.rhevm.api.resource.ReadOnlyDeviceResource;


public class PowerShellReadOnlyDeviceResource<D extends BaseDevice, C extends BaseDevices>
    implements ReadOnlyDeviceResource<D> {

    protected AbstractPowerShellDevicesResource<D, C> parent;
    protected String deviceId;

    public PowerShellReadOnlyDeviceResource(AbstractPowerShellDevicesResource<D, C> parent, String deviceId) {
        this.parent = parent;
        this.deviceId = deviceId;
    }

    @Override
    public D get() {
        List<D> devices = parent.getDevices();

        for (D d : devices) {
            if (deviceId.equals(d.getId())) {
                return parent.addLinks(d);
            }
        }

        return null;
    }
}

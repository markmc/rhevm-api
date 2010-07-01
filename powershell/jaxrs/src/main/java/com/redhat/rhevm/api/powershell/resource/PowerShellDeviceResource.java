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

import java.lang.reflect.Method;
import java.util.List;

import com.redhat.rhevm.api.model.BaseDevice;
import com.redhat.rhevm.api.model.BaseDevices;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.resource.DeviceResource;


public class PowerShellDeviceResource<D extends BaseDevice, C extends BaseDevices>
    implements DeviceResource<D> {

    protected AbstractPowerShellDevicesResource<D, C> parent;
    protected String deviceId;
    protected PowerShellPoolMap shellPools;

    public PowerShellDeviceResource(AbstractPowerShellDevicesResource<D, C> parent, String deviceId, PowerShellPoolMap shellPools) {
        this.parent = parent;
        this.deviceId = deviceId;
        this.shellPools = shellPools;
    }

    public PowerShellCmd getShell() {
        return shellPools.get().get();
    }

    @Override
    public D get() {
        C collection = parent.getDevices();

        String name = collection.getClass().getSimpleName();

        for (Method method : collection.getClass().getMethods()) {
            if (method.getName().equals("get" + name) &&
                List.class.isAssignableFrom(method.getReturnType())) {
                List<D> devices;

                try {
                    devices = asDevices(method.invoke(collection));
                } catch (Exception e) {
                    // invocation target exception should not occur on simple getter
                    continue;
                }

                for (D d : devices) {
                    if (deviceId.equals(d.getId())) {
                        return parent.addLinks(d);
                    }
                }
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private List<D> asDevices(Object o) {
        return (List<D>)o;
    }
}

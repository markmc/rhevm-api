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

import com.redhat.rhevm.api.common.resource.AbstractActionableResource;
import com.redhat.rhevm.api.model.BaseResource;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;

public abstract class AbstractPowerShellActionableResource<R extends BaseResource> extends AbstractActionableResource<R> {

    protected PowerShellPoolMap powerShellPoolMap;

    public AbstractPowerShellActionableResource(String id , PowerShellPoolMap powerShellPoolMap) {
        super(id);
        this.powerShellPoolMap = powerShellPoolMap;
    }

    public AbstractPowerShellActionableResource(String id, Executor executor, PowerShellPoolMap powerShellPoolMap) {
        super(id, executor);
        this.powerShellPoolMap = powerShellPoolMap;
    }

    protected PowerShellCmd getShell() {
        return powerShellPoolMap.get().get();
    }
}

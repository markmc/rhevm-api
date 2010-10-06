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
import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.model.BaseResource;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPool;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;

public abstract class AbstractPowerShellActionableResource<R extends BaseResource> extends AbstractActionableResource<R> {

    protected PowerShellPoolMap shellPools;
    protected PowerShellParser parser;

    public AbstractPowerShellActionableResource(String id , PowerShellPoolMap shellPools) {
        super(id);
        this.shellPools = shellPools;
    }

    public AbstractPowerShellActionableResource(String id, Executor executor, PowerShellPoolMap shellPools) {
        super(id, executor);
        this.shellPools = shellPools;
    }

    public AbstractPowerShellActionableResource(String id, Executor executor, UriInfoProvider uriProvider, PowerShellPoolMap shellPools) {
        super(id, executor, uriProvider);
        this.shellPools = shellPools;
    }

    public AbstractPowerShellActionableResource(String id,
                                                Executor executor,
                                                PowerShellPoolMap shellPools,
                                                PowerShellParser parser) {
        super(id, executor);
        this.shellPools = shellPools;
        this.parser = parser;
    }

    public AbstractPowerShellActionableResource(String id,
                                                Executor executor,
                                                UriInfoProvider uriProvider,
                                                PowerShellPoolMap shellPools,
                                                PowerShellParser parser) {
        super(id, executor, uriProvider);
        this.shellPools = shellPools;
        this.parser = parser;
    }

    protected PowerShellPool getPool() {
        return shellPools.get();
    }

    protected PowerShellParser getParser() {
        return parser;
    }
}

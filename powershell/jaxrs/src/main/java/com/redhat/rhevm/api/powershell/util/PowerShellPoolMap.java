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
package com.redhat.rhevm.api.powershell.util;

import java.util.concurrent.ExecutorService;

import com.redhat.rhevm.api.common.invocation.Current;
import com.redhat.rhevm.api.common.security.Principal;
import com.redhat.rhevm.api.common.util.ReapedMap;

@SuppressWarnings("serial")
public class PowerShellPoolMap extends ReapedMap<Principal, PowerShellPool> {

    private static final long REAP_AFTER = 10 * 60 * 1000L; // 10 minutes

    private ExecutorService executor;
    private Current current;

    public PowerShellPoolMap() {
        super(REAP_AFTER);
    }

    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    public void setCurrent(Current current) {
        this.current = current;
    }

    public synchronized PowerShellPool get() {
        PowerShellPool pool = null;
        Principal principal = current.get(Principal.class);
        if (principal != null) {
            pool = super.get(principal);
            if (pool == null) {
                pool = new PowerShellPool(executor, principal);
                super.put(principal, pool);
                super.reapable(principal);
            }
        }
        return pool;
    }
}

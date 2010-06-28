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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

import com.redhat.rhevm.api.common.security.Principal;

public class PowerShellPool {

    private static final int DEFAULT_POOL_SIZE = 4;

    private ExecutorService executor;
    private Principal principal;

    private BlockingQueue<PowerShellCmd> cmds = new LinkedBlockingQueue<PowerShellCmd>();

    public PowerShellPool(ExecutorService executor, Principal principal, int initialSize) {
        this.executor = executor;
        this.principal = principal;

        for (int i = 0; i < initialSize; i++) {
            executor.execute(new PowerShellLauncher());
        }
    }

    public PowerShellPool(ExecutorService executor, Principal principal) {
        this(executor, principal, DEFAULT_POOL_SIZE);
    }

    public PowerShellCmd get() {
        PowerShellCmd cmd;
        while (true) {
            try {
                cmd = cmds.take();
                break;
            } catch (InterruptedException ex) {
                // ignore and block again
            }
        }
        executor.execute(new PowerShellLauncher());
        return cmd;
    }

    public void add(PowerShellCmd cmd) {
        cmds.offer(cmd);
    }

    public void shutdown() {
        executor.shutdown();

        PowerShellCmd cmd;
        while ((cmd = cmds.poll()) != null) {
            cmd.destroy();
        }
    }

    private class PowerShellLauncher implements Runnable {
        @Override public void run() {
            PowerShellCmd cmd = new PowerShellCmd();
            cmd.start();
            PowerShellPool.this.add(cmd);
        }
    }
}

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

import com.redhat.rhevm.api.common.security.auth.Principal;

public class PowerShellPool {

    // REVISIT: add a timeout after which we will reduce
    //          the pool to the low watermark

    private static final int DEFAULT_SIZE_LOW = 2;
    private static final int DEFAULT_SIZE_HIGH = 6;

    private ExecutorService executor;
    private Principal principal;

    private BlockingQueue<PowerShellCmd> cmds = new LinkedBlockingQueue<PowerShellCmd>();

    private int lowSize;
    private int highSize;
    private int spawned;

    public PowerShellPool(ExecutorService executor, Principal principal, int lowSize, int highSize) {
        this.executor = executor;
        this.principal = principal;
        this.lowSize = lowSize;
        this.highSize = highSize;

        for (int i = 0; i < lowSize; i++) {
            spawn();
        }
    }

    public PowerShellPool(ExecutorService executor, Principal principal) {
        this(executor, principal, DEFAULT_SIZE_LOW, DEFAULT_SIZE_HIGH);
    }

    private void spawn() {
        executor.execute(new PowerShellLauncher());
        ++spawned;
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
        if (cmds.size() < lowSize && spawned < highSize) {
            spawn();
        }
        return cmd;
    }

    public void add(PowerShellCmd cmd) {
        cmds.offer(cmd);
    }

    public void shutdown() {
        executor.shutdown();

        PowerShellCmd cmd;
        while ((cmd = cmds.poll()) != null) {
            cmd.stop();
        }
    }

    private class PowerShellLauncher implements Runnable {
        @Override public void run() {
            PowerShellCmd cmd = new PowerShellCmd(principal);
            cmd.start();
            PowerShellPool.this.add(cmd);
        }
    }
}

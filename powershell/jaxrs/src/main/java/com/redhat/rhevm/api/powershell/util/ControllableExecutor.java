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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * A simple executor for testing purposes that allows finegrained control
 * of when a task is run.
 */
public class ControllableExecutor extends AbstractExecutorService {

    private List<Runnable> tasks;
    private boolean shutdown = false;

    public ControllableExecutor() {
        tasks = new LinkedList<Runnable>();
    }

    @Override
    public synchronized void execute(Runnable command) {
        if (shutdown) {
            throw new RejectedExecutionException("executor is shutdown");
        }
        tasks.add(tasks.size(), command);
    }

    public void runNext() {
        Runnable task = null;
        synchronized (this) {
            if (!tasks.isEmpty()) {
                task = tasks.remove(0);
            }
        }
        if (task != null) {
            task.run();
        }
    }

    public synchronized int taskCount() {
        return tasks.size();
    }

    private void awaitTermination() {
        while (taskCount() > 0) {
            runNext();
        }
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) {
        awaitTermination();
        return true;
    }

    @Override
    public boolean isTerminated() {
        return isShutdown() && taskCount() == 0;
    }

    @Override
    public synchronized boolean isShutdown() {
        return shutdown;
    }

    @Override
    public synchronized void shutdown() {
        shutdown = true;
    }

    @Override
    public List<Runnable> shutdownNow() {
        shutdown();
        awaitTermination();
        return null;
    }
}

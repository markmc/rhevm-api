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

import org.junit.Assert;
import org.junit.Test;

public class ControllableExecutorTest extends Assert {
    @Test
    public void test() {
        ControllableExecutor executor = new ControllableExecutor();

        executor.execute(new DoNothing());
        executor.execute(new DoNothing());
        executor.execute(new DoNothing());

        assertEquals(3, executor.taskCount());

        executor.runNext();

        assertEquals(2, executor.taskCount());

        executor.shutdown();

        assert(executor.isShutdown());

        executor.shutdownNow();

        assert(executor.isTerminated());
        assertEquals(0, executor.taskCount());
    }

    private static class DoNothing implements Runnable {
        @Override
        public void run() {
        }
    }

}

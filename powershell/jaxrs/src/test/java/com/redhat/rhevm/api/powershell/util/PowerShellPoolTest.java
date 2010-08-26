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

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import org.junit.runner.RunWith;

import static org.easymock.classextension.EasyMock.expectLastCall;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import com.redhat.rhevm.api.common.invocation.Current;
import com.redhat.rhevm.api.common.security.auth.Principal;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PowerShellPool.class})
public class PowerShellPoolTest extends Assert {

    @After
    public void tearDown() {
        verifyAll();
    }

    @Test
    public void test() throws Exception {
        Principal principal = new Principal("joe", "schmoe");

        ControllableExecutor executor = new ControllableExecutor();

        PowerShellPool pool = new PowerShellPool(executor, principal, setupExpectations(principal), 10, 20);

        assertEquals(10, executor.taskCount());

        for (int i = 0; i < 5; i++) {
            executor.runNext();
        }

        assertEquals(5, executor.taskCount());

        PowerShellCmd cmd = pool.get();
        assertNotNull(cmd);

        assertEquals(6, executor.taskCount());

        for (int i = 0; i < 6; i++) {
            executor.runNext();
        }

        assertEquals(0, executor.taskCount());

        pool.shutdown();
    }

    private Current setupExpectations(Principal principal) throws Exception {
        PowerShellCmd cmd = createMock(PowerShellCmd.class);
        Current current = createMock(Current.class);
        expectNew(PowerShellCmd.class, principal, current).andReturn(cmd).anyTimes();
        cmd.start();
        expectLastCall().anyTimes();
        cmd.stop();
        expectLastCall().anyTimes();
        replayAll();
        return current;
    }
}

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

import com.redhat.rhevm.api.model.BaseResource;
import com.redhat.rhevm.api.powershell.util.ControllableExecutor;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPool;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;

import org.junit.runner.RunWith;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.verifyAll;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { PowerShellCmd.class })
@Ignore
public abstract class AbstractPowerShellSimpleResourceTest<R extends BaseResource,
                                                           A extends AbstractPowerShellResource>
    extends BasePowerShellResourceTest {

    protected A resource;
    protected ControllableExecutor executor;
    protected PowerShellPoolMap poolMap;
    protected PowerShellParser parser;

    @Before
    public void setUp() throws Exception {
        executor = new ControllableExecutor();
        poolMap = createMock(PowerShellPoolMap.class);
        parser = PowerShellParser.newInstance();
        resource = getResource(executor, poolMap, parser);
    }

    @After
    public void tearDown() {
        verifyAll();
    }

    protected PowerShellPool setUpPoolExpectations() {
        return setUpPoolExpectations(1);
    }

    protected PowerShellPool setUpPoolExpectations(int times) {
        PowerShellPool pool = createMock(PowerShellPool.class);
        expect(poolMap.get()).andReturn(pool).times(times);
        return pool;
    }

    protected abstract A getResource(Executor executor, PowerShellPoolMap poolMap, PowerShellParser parser);
}

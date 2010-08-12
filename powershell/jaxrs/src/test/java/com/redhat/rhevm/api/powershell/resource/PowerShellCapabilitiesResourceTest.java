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

import org.junit.Before;
import org.junit.Test;

import com.redhat.rhevm.api.model.Capabilities;
import com.redhat.rhevm.api.model.CPU;
import com.redhat.rhevm.api.resource.CapabilitiesResource;
import com.redhat.rhevm.api.common.resource.DefaultCapabilitiesResource;

public class PowerShellCapabilitiesResourceTest extends BasePowerShellResourceTest {

    protected CapabilitiesResource resource;

    @Before
    public void setUp() throws Exception {
        resource = new DefaultCapabilitiesResource();
    }

    private void checkCpu(CPU cpu) {
        assertNotNull(cpu.getId());
        assertNotNull(cpu.getLevel());
        assertNotNull(cpu.getFlags());
        assertNotNull(cpu.getFlags().getFlags().size() > 0);
    }

    @Test
    public void testGet() {
        Capabilities caps = resource.get();

        assertNotNull(caps);
        assertNotNull(caps.getCPUs());
        assertNotNull(caps.getCPUs().getCPUs());
        assertTrue(caps.getCPUs().getCPUs().size() > 0);

        for (CPU cpu : caps.getCPUs().getCPUs()) {
            checkCpu(cpu);
        }
    }
}

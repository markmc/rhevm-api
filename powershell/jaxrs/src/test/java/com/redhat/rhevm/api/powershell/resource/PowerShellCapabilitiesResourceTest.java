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
import com.redhat.rhevm.api.model.SchedulingPolicies;
import com.redhat.rhevm.api.model.SchedulingPolicyType;
import com.redhat.rhevm.api.model.VersionCaps;
import com.redhat.rhevm.api.model.VmType;
import com.redhat.rhevm.api.model.VmTypes;
import com.redhat.rhevm.api.common.resource.DefaultCapabilitiesResource;

public class PowerShellCapabilitiesResourceTest extends BasePowerShellResourceTest {

    protected Capabilities caps;

    @Before
    public void setUp() throws Exception {
        caps = new DefaultCapabilitiesResource().get();
    }

    private void checkCpu(CPU cpu) {
        assertNotNull(cpu.getId());
        assertNotNull(cpu.getLevel());
    }

    private void checkVersion(VersionCaps version, int major, int minor, boolean current) {
        assertEquals(major, version.getMajor());
        assertEquals(minor, version.getMinor());
        assertEquals(current, version.isCurrent());

        assertNotNull(version.getCPUs());
        assertNotNull(version.getCPUs().getCPUs());
        assertTrue(version.getCPUs().getCPUs().size() > 0);

        for (CPU cpu : version.getCPUs().getCPUs()) {
            checkCpu(cpu);
        }
    }

    private void checkSchedulingPolicies(SchedulingPolicies policies, SchedulingPolicyType... expected) {
        assertEquals(expected.length, policies.getPolicy().size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i].value(), policies.getPolicy().get(i));
        }
    }

    private void checkVmTypes(VmTypes vmTypes, VmType... expected) {
        assertNotNull(vmTypes);
        assertEquals(expected.length, vmTypes.getVmTypes().size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i].value(), vmTypes.getVmTypes().get(i));
        }
    }

    @Test
    public void testCaps() {
        assertNotNull(caps);
        assertNotNull(caps.getVersions());
        assertEquals(2, caps.getVersions().size());
    }

    @Test
    public void test22Caps() {
        checkVersion(caps.getVersions().get(0), 2, 2, true);
    }

    @Test
    public void test21Caps() {
        checkVersion(caps.getVersions().get(1), 2, 1, false);
    }

    @Test
    public void testSchedulingPolicies() {
        assertNotNull(caps.getSchedulingPolicies());
        checkSchedulingPolicies(caps.getSchedulingPolicies(), SchedulingPolicyType.values());
    }

    @Test
    public void testVmTypes() {
        checkVmTypes(caps.getVersions().get(0).getVmTypes(), VmType.values());
        checkVmTypes(caps.getVersions().get(1).getVmTypes(), VmType.values());
    }
}

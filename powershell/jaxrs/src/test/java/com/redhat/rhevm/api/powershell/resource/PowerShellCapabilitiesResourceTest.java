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
import com.redhat.rhevm.api.model.DiskType;
import com.redhat.rhevm.api.model.DiskTypes;
import com.redhat.rhevm.api.model.DiskFormat;
import com.redhat.rhevm.api.model.DiskFormats;
import com.redhat.rhevm.api.model.FenceType;
import com.redhat.rhevm.api.model.FenceTypes;
import com.redhat.rhevm.api.model.NicType;
import com.redhat.rhevm.api.model.NicTypes;
import com.redhat.rhevm.api.model.SchedulingPolicies;
import com.redhat.rhevm.api.model.SchedulingPolicyType;
import com.redhat.rhevm.api.model.VersionCaps;
import com.redhat.rhevm.api.model.StorageDomainType;
import com.redhat.rhevm.api.model.StorageDomainTypes;
import com.redhat.rhevm.api.model.StorageType;
import com.redhat.rhevm.api.model.StorageTypes;
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

    private void checkStorageTypes(StorageTypes storageTypes, StorageType... expected) {
        assertNotNull(storageTypes);
        assertEquals(expected.length, storageTypes.getStorageTypes().size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i].value(), storageTypes.getStorageTypes().get(i));
        }
    }

    private void checkStorageDomainTypes(StorageDomainTypes storageDomainTypes, StorageDomainType... expected) {
        assertNotNull(storageDomainTypes);
        assertEquals(expected.length, storageDomainTypes.getStorageDomainTypes().size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i].value(), storageDomainTypes.getStorageDomainTypes().get(i));
        }
    }

    private void checkFenceTypes(FenceTypes fenceTypes, FenceType... expected) {
        assertNotNull(fenceTypes);
        assertEquals(expected.length, fenceTypes.getFenceTypes().size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i].value(), fenceTypes.getFenceTypes().get(i));
        }
    }

    private void checkNicTypes(NicTypes nicTypes, NicType... expected) {
        assertNotNull(nicTypes);
        assertEquals(expected.length, nicTypes.getNicTypes().size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i].value(), nicTypes.getNicTypes().get(i));
        }
    }

    private void checkDiskTypes(DiskTypes disktypes, DiskType... expected) {
        assertNotNull(disktypes);
        assertEquals(expected.length, disktypes.getDiskTypes().size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i].value(), disktypes.getDiskTypes().get(i));
        }
    }

    private void checkDiskFormats(DiskFormats nicFormats, DiskFormat... expected) {
        assertNotNull(nicFormats);
        assertEquals(expected.length, nicFormats.getDiskFormats().size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i].value(), nicFormats.getDiskFormats().get(i));
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

    @Test
    public void testStorageTypes() {
        checkStorageTypes(caps.getVersions().get(0).getStorageTypes(), StorageType.ISCSI, StorageType.FCP, StorageType.NFS);
        checkStorageTypes(caps.getVersions().get(1).getStorageTypes(), StorageType.ISCSI, StorageType.FCP, StorageType.NFS);
    }

    @Test
    public void testStorageDomainTypes() {
        checkStorageDomainTypes(caps.getVersions().get(0).getStorageDomainTypes(), StorageDomainType.values());
        checkStorageDomainTypes(caps.getVersions().get(1).getStorageDomainTypes(), StorageDomainType.values());
    }

    @Test
    public void testFenceTypes() {
        checkFenceTypes(caps.getVersions().get(0).getFenceTypes(), FenceType.values());
        checkFenceTypes(caps.getVersions().get(1).getFenceTypes(), FenceType.values());
    }

    @Test
    public void testNicTypes() {
        checkNicTypes(caps.getVersions().get(0).getNicTypes(), NicType.values());
        checkNicTypes(caps.getVersions().get(1).getNicTypes(), NicType.values());
    }

    @Test
    public void testDiskTypes() {
        checkDiskTypes(caps.getVersions().get(0).getDiskTypes(), DiskType.values());
        checkDiskTypes(caps.getVersions().get(1).getDiskTypes(), DiskType.values());
    }

    @Test
    public void testDiskFormats() {
        checkDiskFormats(caps.getVersions().get(0).getDiskFormats(), DiskFormat.values());
        checkDiskFormats(caps.getVersions().get(1).getDiskFormats(), DiskFormat.values());
    }
}

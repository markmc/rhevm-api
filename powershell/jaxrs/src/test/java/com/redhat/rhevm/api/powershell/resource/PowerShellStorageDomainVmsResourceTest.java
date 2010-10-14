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

import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.model.VMs;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;

import org.junit.Test;

import static org.powermock.api.easymock.PowerMock.replayAll;

public class PowerShellStorageDomainVmsResourceTest
    extends AbstractPowerShellStorageDomainContentsResourceTest<VMs, VM, PowerShellStorageDomainVmsResource> {

    protected static final String COLLECTION_URI = STORAGE_DOMAIN_URI + SLASH + "vms";

    private static final String GET_VMS_COMMAND = "$sd = get-storagedomain \"" + STORAGE_DOMAIN_ID + "\"; if ($sd.domaintype -eq \"Data\") { get-vm -storagedomainid \"" + STORAGE_DOMAIN_ID + "\" } elseif ($sd.domaintype -eq \"Export\") { get-vmimportcandidates -showall -datacenterid \"" + DATA_CENTER_ID + "\" -storagedomainid \"" + STORAGE_DOMAIN_ID + "\" }";

    private static final String GET_VM_COMMAND = "$sd = get-storagedomain \"" + STORAGE_DOMAIN_ID + "\"; if ($sd.domaintype -eq \"Data\") { get-vm -vmid \"" + asId(NAMES[0]) + "\" } elseif ($sd.domaintype -eq \"Export\") { get-vmimportcandidates -showall -datacenterid \"" + DATA_CENTER_ID + "\" -storagedomainid \"" + STORAGE_DOMAIN_ID + "\" | ? { $_.vmid -eq \"" + asId(NAMES[0]) + "\" } }";

    protected PowerShellStorageDomainVmsResource getResource(PowerShellAttachedStorageDomainResource parent,
                                                             PowerShellPoolMap poolMap,
                                                             PowerShellParser parser) {
        return new PowerShellStorageDomainVmsResource(parent, poolMap, parser);
    }

    protected String formatVms(String[] names) {
        String[] descriptions = new String[names.length];
        return formatXmlReturn("vm", names, descriptions, PowerShellVmsResourceTest.extraArgs);
    }

    protected String formatVm(String name) {
        return formatVms(asArray(name));
    }

    @Test
    public void testGetList() {
        setUpCmdExpectations(GET_VMS_COMMAND, formatVms(NAMES));
        setUriInfo(setUpBasicUriExpectations());
        replayAll();
        verifyVms(resource.list());
    }

    @Test
    public void testGet() {
        PowerShellStorageDomainVmResource childResource = new
            PowerShellStorageDomainVmResource(resource,
                                              asId(NAMES[0]),
                                              executor,
                                              uriProvider,
                                              poolMap,
                                              parser);
        setUpCmdExpectations(GET_VM_COMMAND, formatVm(NAMES[0]));
        setUriInfo(setUpBasicUriExpectations());
        replayAll();
        verifyVm(childResource.get(), 0);
    }

    protected void verifyVms(VMs vms) {
        assertNotNull(vms.getVMs());
        assertEquals("unexpected collection size", vms.getVMs().size(), NAMES.length);
        for (int i = 0; i < NAMES.length; i++) {
            verifyVm(vms.getVMs().remove(0), i);
        }
    }

    protected void verifyVm(VM vm, int index) {
        assertEquals(asId(NAMES[index]), vm.getId());
        assertEquals(NAMES[index], vm.getName());
        assertEquals(COLLECTION_URI + SLASH + asId(NAMES[index]), vm.getHref());
        assertNotNull(vm.getStorageDomain());
        assertEquals(STORAGE_DOMAIN_ID, vm.getStorageDomain().getId());
        assertEquals(STORAGE_DOMAIN_URI, vm.getStorageDomain().getHref());
        assertFalse(vm.getStorageDomain().isSetDataCenter());
    }
}

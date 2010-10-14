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

import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.model.VMs;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPool;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;

import org.junit.Test;

import org.junit.runner.RunWith;

import static org.easymock.classextension.EasyMock.expect;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { PowerShellCmd.class })
public class PowerShellStorageDomainVmsResourceTest
    extends AbstractPowerShellResourceTest<VM, PowerShellStorageDomainVmsResource> {

    private static final String[] VMS = {"clontarf", "killester", "harmonstown", "raheny", "kilbarrack"};

    private static final String DATA_CENTER_NAME = "sutton";
    private static final String DATA_CENTER_ID = asId(DATA_CENTER_NAME);
    private static final String STORAGE_DOMAIN_NAME = "howth";
    private static final String STORAGE_DOMAIN_ID = asId(STORAGE_DOMAIN_NAME);

    private static final String STORAGE_DOMAIN_URI = BASE_PATH + SLASH + "datacenters" + SLASH + DATA_CENTER_ID + SLASH + "storagedomains" + SLASH + STORAGE_DOMAIN_ID;
    private static final String COLLECTION_URI = STORAGE_DOMAIN_URI + SLASH + "vms";

    private static final String GET_VMS_COMMAND = "$sd = get-storagedomain \"" + STORAGE_DOMAIN_ID + "\"; if ($sd.domaintype -eq \"Data\") { get-vm -storagedomainid \"" + STORAGE_DOMAIN_ID + "\" } elseif ($sd.domaintype -eq \"Export\") { get-vmimportcandidates -showall -datacenterid \"" + DATA_CENTER_ID + "\" -storagedomainid \"" + STORAGE_DOMAIN_ID + "\" }";

    private static final String GET_VM_COMMAND = "$sd = get-storagedomain \"" + STORAGE_DOMAIN_ID + "\"; if ($sd.domaintype -eq \"Data\") { get-vm -vmid \"" + asId(VMS[0]) + "\" } elseif ($sd.domaintype -eq \"Export\") { get-vmimportcandidates -showall -datacenterid \"" + DATA_CENTER_ID + "\" -storagedomainid \"" + STORAGE_DOMAIN_ID + "\" | ? { $_.vmid -eq \"" + asId(VMS[0]) + "\" } }";

    protected PowerShellStorageDomainVmsResource getResource(Executor executor,
                                                             PowerShellPoolMap poolMap,
                                                             PowerShellParser parser,
                                                             UriInfoProvider uriProvider) {
        PowerShellAttachedStorageDomainsResource grandParent =
            new PowerShellAttachedStorageDomainsResource(DATA_CENTER_ID, poolMap, parser);

        PowerShellAttachedStorageDomainResource parent =
            new PowerShellAttachedStorageDomainResource(grandParent,
                                                        STORAGE_DOMAIN_ID,
                                                        executor,
                                                        uriProvider,
                                                        poolMap,
                                                        parser);

        return new PowerShellStorageDomainVmsResource(parent, poolMap, parser);
    }

    protected void setUriInfo(UriInfo uriInfo) {
        super.setUriInfo(uriInfo);
        resource.setUriInfo(uriInfo);
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
        setUpCmdExpectations(GET_VMS_COMMAND, formatVms(VMS));
        setUriInfo(setUpBasicUriExpectations());
        replayAll();
        verifyVms(resource.list());
    }

    @Test
    public void testGet() {
        PowerShellStorageDomainVmResource childResource = new
            PowerShellStorageDomainVmResource(resource,
                                              asId(VMS[0]),
                                              executor,
                                              uriProvider,
                                              poolMap,
                                              parser);
        setUpCmdExpectations(GET_VM_COMMAND, formatVm(VMS[0]));
        setUriInfo(setUpBasicUriExpectations());
        replayAll();
        verifyVm(childResource.get(), 0);
    }

    private void setUpCmdExpectations(String command, String ret) {
        mockStatic(PowerShellCmd.class);
        expect(PowerShellCmd.runCommand(setUpPoolExpectations(), command)).andReturn(ret);
    }

    protected void verifyVms(VMs vms) {
        assertNotNull(vms.getVMs());
        assertEquals("unexpected collection size", vms.getVMs().size(), VMS.length);
        for (int i = 0; i < VMS.length; i++) {
            verifyVm(vms.getVMs().remove(0), i);
        }
    }

    protected void verifyVm(VM vm, int index) {
        assertEquals(asId(VMS[index]), vm.getId());
        assertEquals(VMS[index], vm.getName());
        assertEquals(COLLECTION_URI + SLASH + asId(VMS[index]), vm.getHref());
        assertNotNull(vm.getStorageDomain());
        assertEquals(STORAGE_DOMAIN_ID, vm.getStorageDomain().getId());
        assertEquals(STORAGE_DOMAIN_URI, vm.getStorageDomain().getHref());
        assertFalse(vm.getStorageDomain().isSetDataCenter());
    }
}

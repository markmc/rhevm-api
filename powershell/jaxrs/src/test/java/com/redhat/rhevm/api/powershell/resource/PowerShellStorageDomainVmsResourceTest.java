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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.model.VMs;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;

import org.junit.Ignore;
import org.junit.Test;

import static org.powermock.api.easymock.PowerMock.replayAll;

@Ignore
public class PowerShellStorageDomainVmsResourceTest
    extends AbstractPowerShellStorageDomainContentsResourceTest<VMs, VM, PowerShellStorageDomainVmsResource> {

    protected static final String COLLECTION_URI = STORAGE_DOMAIN_URI + SLASH + "vms";

    private static final String GET_VMS_COMMAND = "$sd = get-storagedomain \"" + STORAGE_DOMAIN_ID + "\"; if ($sd.domaintype.StartsWith(\"Data\")) { get-vm -storagedomainid \"" + STORAGE_DOMAIN_ID + "\" } elseif ($sd.domaintype -eq \"Export\") { get-vmimportcandidates -showall -datacenterid \"" + DATA_CENTER_ID + "\" -storagedomainid \"" + STORAGE_DOMAIN_ID + "\" }";

    private static final String GET_VM_COMMAND = "$sd = get-storagedomain \"" + STORAGE_DOMAIN_ID + "\"; if ($sd.domaintype.StartsWith(\"Data\")) { get-vm -vmid \"" + asId(NAMES[0]) + "\" } elseif ($sd.domaintype -eq \"Export\") { get-vmimportcandidates -showall -datacenterid \"" + DATA_CENTER_ID + "\" -storagedomainid \"" + STORAGE_DOMAIN_ID + "\" | ? { $_.vmid -eq \"" + asId(NAMES[0]) + "\" } }";

    protected static final String IMPORT_VM_COMMAND = "import-vm -datacenterid \"" + DATA_CENTER_ID + "\" -sourcedomainid \"" + STORAGE_DOMAIN_ID + "\" -destdomainid \"" + IMPORT_DEST_DOMAIN_ID + "\" -clusterid \"" + IMPORT_CLUSTER_ID + "\" -vmid \"" + asId(NAMES[0]) + "\"";

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
        PowerShellStorageDomainVmResource subResource = getSubResource(0);
        setUpCmdExpectations(GET_VM_COMMAND, formatVm(NAMES[0]));
        setUriInfo(setUpBasicUriExpectations());
        replayAll();
        verifyVm(subResource.get(), 0);
    }

    protected void doTestImport(boolean async) throws Exception {
        PowerShellStorageDomainVmResource subResource = getSubResource(0);

        setUriInfo(setUpActionExpectation("import", IMPORT_VM_COMMAND, 0));

        Action action = getAction(async);
        action.setCluster(new Cluster());
        action.getCluster().setId(IMPORT_CLUSTER_ID);
        action.setStorageDomain(new StorageDomain());
        action.getStorageDomain().setId(IMPORT_DEST_DOMAIN_ID);

        verifyActionResponse(subResource.doImport(action), async, 0);
    }

    @Test
    public void doTestImport() throws Exception {
        doTestImport(false);
    }

    @Test
    public void testImportAsync() throws Exception {
        doTestImport(true);
    }

    @Test
    public void testIncompleteImport() throws Exception {
        PowerShellStorageDomainVmResource subResource = getSubResource(0);

        setUriInfo(setUpActionExpectation(null, null, null, null));
        try {
            subResource.doImport(getAction());
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "Action", "doImport", "cluster.id|name, storageDomain.id|name");
        }
    }

    protected PowerShellStorageDomainVmResource getSubResource(int index) {
        return new PowerShellStorageDomainVmResource(resource,
                                                     asId(NAMES[index]),
                                                     executor,
                                                     uriProvider,
                                                     poolMap,
                                                     parser);
    }

    protected UriInfo setUpActionExpectation(String verb, String command, int index) throws Exception {
        return setUpActionExpectation(COLLECTION_URI + SLASH + asId(NAMES[index]), verb, command, null);
    }

    private void verifyActionResponse(Response r, boolean async, int index) throws Exception {
        verifyActionResponse(r, COLLECTION_URI + SLASH + asId(NAMES[index]), async);
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

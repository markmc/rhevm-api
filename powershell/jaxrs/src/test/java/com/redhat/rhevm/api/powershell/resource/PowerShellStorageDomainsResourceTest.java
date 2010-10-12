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

import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.model.Storage;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.StorageDomainType;
import com.redhat.rhevm.api.model.StorageType;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;

import org.junit.Test;

import org.junit.runner.RunWith;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { PowerShellCmd.class })
public class PowerShellStorageDomainsResourceTest
    extends AbstractPowerShellCollectionResourceTest<StorageDomain, PowerShellStorageDomainResource, PowerShellStorageDomainsResource> {

    private static final String HOST_NAME = "bluegrass";
    private static final String HOST_ID = asId(HOST_NAME);

    private static final String NFS_ADDRESS = "myfiler.mycorp.com";
    private static final String NFS_PATH = "/mystoragedomains/data";

    private static final String GET_HOST_COMMAND = "$h = select-host -searchtext \"name=" + HOST_NAME + "\";";

    private static final String GET_PRECONFIGURED_COMMAND = "$sd = get-preconfiguredstoragedomains -hostid \"" + HOST_ID + "\" -storagedomaintype Data -storagetype NFS -nfsmountpoint \"" + NFS_ADDRESS + ":" + NFS_PATH + "\"; ";
    private static final String IMPORT_PRECONFIGURED_COMMAND = "import-preconfiguredstoragedomain -hostid \"" + HOST_ID + "\" -storagedomainobject $sd";

    private static final String GET_PRECONFIGURED_WITH_HOST_NAME_COMMAND = "$sd = get-preconfiguredstoragedomains -hostid $h.hostid -storagedomaintype Export -storagetype NFS -nfsmountpoint \"" + NFS_ADDRESS + ":" + NFS_PATH + "\"; ";
    private static final String IMPORT_PRECONFIGURED_WITH_HOST_NAME_COMMAND = "import-preconfiguredstoragedomain -hostid $h.hostid -storagedomainobject $sd";

    private static final String NFS_ADD_EPILOG = " -domaintype Data -storagetype NFS -storage \"" + NFS_ADDRESS + ":" + NFS_PATH + "\"";

    private static final String ADD_NFS_COMMAND = "add-storagedomain -name \"" + NEW_NAME + "\" -hostid \"" + HOST_ID + "\"" + NFS_ADD_EPILOG;
    private static final String ADD_NFS_WITH_HOST_NAME_COMMAND = GET_HOST_COMMAND +  "add-storagedomain -name \"" + NEW_NAME + "\" -hostid $h.hostid" + NFS_ADD_EPILOG;
    private static final String ADD_EXISTING_NFS_COMMAND = GET_HOST_COMMAND + GET_PRECONFIGURED_WITH_HOST_NAME_COMMAND + "if ($sd -ne $null) { " + IMPORT_PRECONFIGURED_WITH_HOST_NAME_COMMAND + " } else { throw \"A name is require when creating a new storage domain\" }";

    protected static final String[] NULL_DESCRIPTIONS = { null, null, null };
    protected static final String[] NULL_DESCRIPTIONS_SUBSET = { null, null };

    public static final String[] extraArgs = new String[] { };

    public PowerShellStorageDomainsResourceTest() {
        super(new PowerShellStorageDomainResource("0", new PowerShellStorageDomainsResource(), null, null), "storagedomains", "storagedomain", extraArgs);
    }

    @Test
    public void testList() throws Exception {
        String [] commands = { getSelectCommand(), };
        String [] returns = { getSelectReturn(), };

        resource.setUriInfo(setUpResourceExpectations(commands, returns, null, NAMES));
        verifyCollection(
            resource.list().getStorageDomains(),
            NAMES, NULL_DESCRIPTIONS);
    }

    @Test
    public void testQuery() throws Exception {
        String [] commands = { getQueryCommand(StorageDomain.class), };
        String [] returns = { getQueryReturn(), };

        resource.setUriInfo(setUpResourceExpectations(commands, returns, getQueryParam(), NAMES_SUBSET));
        verifyCollection(resource.list().getStorageDomains(), NAMES_SUBSET, NULL_DESCRIPTIONS_SUBSET);
    }

    @Test
    public void testNfsAdd() throws Exception {
        StorageDomain model = getModel(NEW_NAME, NEW_DESCRIPTION);
        model.setHost(new Host());
        model.getHost().setId(HOST_ID);
        model.setType(StorageDomainType.DATA);
        model.setStorage(new Storage());
        model.getStorage().setType(StorageType.NFS);
        model.getStorage().setAddress(NFS_ADDRESS);
        model.getStorage().setPath(NFS_PATH);
        resource.setUriInfo(setUpAddResourceExpectations(ADD_NFS_COMMAND, getAddReturn(), NEW_NAME));
        verifyResponse(resource.add(model), NEW_NAME, null);
    }

    @Test
    public void testNfsAddWithHostName() throws Exception {
        StorageDomain model = getModel(NEW_NAME, NEW_DESCRIPTION);
        model.setHost(new Host());
        model.getHost().setName(HOST_NAME);
        model.setType(StorageDomainType.DATA);
        model.setStorage(new Storage());
        model.getStorage().setType(StorageType.NFS);
        model.getStorage().setAddress(NFS_ADDRESS);
        model.getStorage().setPath(NFS_PATH);
        resource.setUriInfo(setUpAddResourceExpectations(ADD_NFS_WITH_HOST_NAME_COMMAND, getAddReturn(), NEW_NAME));
        verifyResponse(resource.add(model), NEW_NAME, null);
    }

    @Test
    public void testNfsAddExisting() throws Exception {
        StorageDomain model = getModel(null, null);
        model.setHost(new Host());
        model.getHost().setName(HOST_NAME);
        model.setType(StorageDomainType.EXPORT);
        model.setStorage(new Storage());
        model.getStorage().setType(StorageType.NFS);
        model.getStorage().setAddress(NFS_ADDRESS);
        model.getStorage().setPath(NFS_PATH);
        resource.setUriInfo(setUpAddResourceExpectations(ADD_EXISTING_NFS_COMMAND, getAddReturn(), NEW_NAME));
        verifyResponse(resource.add(model), NEW_NAME, null);
    }

    protected PowerShellStorageDomainsResource getResource() {
	return new PowerShellStorageDomainsResource();
    }

    protected void populateModel(StorageDomain storageDomain) {
    }
}

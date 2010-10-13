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

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.StorageDomainStatus;

import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;

import org.junit.Test;

import static org.easymock.classextension.EasyMock.expect;

import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;


public class PowerShellAttachedStorageDomainResourceTest
    extends AbstractPowerShellResourceTest<StorageDomain, PowerShellAttachedStorageDomainResource> {

    private static final String DATA_CENTER_NAME = "d12345";
    private static final String DATA_CENTER_ID = Integer.toString(DATA_CENTER_NAME.hashCode());
    private static final String STORAGE_DOMAIN_NAME = "d12345";
    private static final String STORAGE_DOMAIN_ID = Integer.toString(STORAGE_DOMAIN_NAME.hashCode());
    private static final String DATA_CENTER_URI = "datacenters/" + DATA_CENTER_ID;
    private static final String STORAGE_DOMAIN_URI = "storagedomains/" + STORAGE_DOMAIN_ID;
    private static final String ATTACHED_STORAGE_DOMAIN_URI = DATA_CENTER_URI + "/" + STORAGE_DOMAIN_URI;
    private static final String DEACTIVATE_ACTION_URI = ATTACHED_STORAGE_DOMAIN_URI + "/deactivate";
    private static final String DATA_CENTER_ARG = " -datacenterid \"" + DATA_CENTER_ID + "\"";
    private static final String STORAGE_DOMAIN_ARG = " -storagedomainid \"" + STORAGE_DOMAIN_ID + "\"";
    private static final String DC_AND_SD_ARGS = DATA_CENTER_ARG + STORAGE_DOMAIN_ARG;
    private static final String GET_COMMAND = "get-storagedomain" + DC_AND_SD_ARGS;
    private static final String ACTION_RETURN = "replace with realistic powershell return";

    protected PowerShellAttachedStorageDomainResource getResource(Executor executor,
                                                                  PowerShellPoolMap poolMap,
                                                                  PowerShellParser parser,
                                                                  UriInfoProvider uriProvider) {
        return new PowerShellAttachedStorageDomainResource(new PowerShellAttachedStorageDomainsResource(DATA_CENTER_ID,
                                                                                                        poolMap,
                                                                                                        parser),
                                                           STORAGE_DOMAIN_ID,
                                                           executor,
                                                           uriProvider,
                                                           poolMap,
                                                           parser);
    }

    protected String formatStorageDomain(String name) {
        return formatXmlReturn("storagedomain", new String[] { name }, new String[] { "" }, new String[] {});
    }

    @Test
    public void testGet() throws Exception {
        setUriInfo(setUpAttachedStorageDomainExpectations());
        verifyAttachedStorageDomain(resource.get());
    }

    @Test
    public void testActivate() throws Exception {
        setUriInfo(setUpActionExpectation("activate", "activate-storagedomain"));
        verifyActionResponse(resource.activate(getAction()), false);
    }

    @Test
    public void testDeactivate() throws Exception {
        setUriInfo(setUpActionExpectation("deactivate", "deactivate-storagedomain"));
        verifyActionResponse(resource.deactivate(getAction()), false);
    }

    @Test
    public void testActivateAsync() throws Exception {
        setUriInfo(setUpActionExpectation("activate", "activate-storagedomain"));
        verifyActionResponse(resource.activate(getAction(true)), true);
    }

    @Test
    public void testDeactivateAsync() throws Exception {
        setUriInfo(setUpActionExpectation("deactivate", "deactivate-storagedomain"));
        verifyActionResponse(resource.deactivate(getAction(true)), true);
    }

    private UriInfo setUpAttachedStorageDomainExpectations() throws Exception {
        mockStatic(PowerShellCmd.class);
        expect(PowerShellCmd.runCommand(setUpPoolExpectations(),
                                        GET_COMMAND)).andReturn(formatStorageDomain(STORAGE_DOMAIN_NAME));
        UriInfo uriInfo = setUpBasicUriExpectations();
        replayAll();
        return uriInfo;
    }

    private UriInfo setUpActionExpectation(String verb, String command) throws Exception {
        return setUpActionExpectation(ATTACHED_STORAGE_DOMAIN_URI, verb, command + DC_AND_SD_ARGS, ACTION_RETURN);
    }

    private void verifyAttachedStorageDomain(StorageDomain storageDomain) {
        assertNotNull(storageDomain);
        assertEquals(storageDomain.getId(), STORAGE_DOMAIN_ID);
        assertEquals(storageDomain.getName(), STORAGE_DOMAIN_NAME);
        assertNotNull(storageDomain.getDataCenter());
        assertEquals(storageDomain.getDataCenter().getId(), DATA_CENTER_ID);
        assertEquals(storageDomain.getStatus(), StorageDomainStatus.ACTIVE);
        assert(storageDomain.isMaster());
        verifyLinks(storageDomain);
    }

    private void verifyActionResponse(Response r, boolean async) throws Exception {
        verifyActionResponse(r, ATTACHED_STORAGE_DOMAIN_URI, async);
    }
}

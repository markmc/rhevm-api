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
import com.redhat.rhevm.api.model.BaseResource;
import com.redhat.rhevm.api.model.BaseResources;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;

import org.junit.runner.RunWith;

import static org.easymock.classextension.EasyMock.expect;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.powermock.api.easymock.PowerMock.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { PowerShellCmd.class })
public abstract class AbstractPowerShellStorageDomainContentsResourceTest<C extends BaseResources,
                                                                          R extends BaseResource,
                                                                          A extends AbstractPowerShellStorageDomainContentsResource<R>>
    extends AbstractPowerShellResourceTest<R, A> {

    protected static final String[] NAMES = {"clontarf", "killester", "harmonstown", "raheny", "kilbarrack"};

    protected static final String DATA_CENTER_NAME = "sutton";
    protected static final String DATA_CENTER_ID = asId(DATA_CENTER_NAME);
    protected static final String STORAGE_DOMAIN_NAME = "howth";
    protected static final String STORAGE_DOMAIN_ID = asId(STORAGE_DOMAIN_NAME);

    protected static final String STORAGE_DOMAIN_URI = BASE_PATH + SLASH + "datacenters" + SLASH + DATA_CENTER_ID + SLASH + "storagedomains" + SLASH + STORAGE_DOMAIN_ID;

    protected static final String IMPORT_CLUSTER_ID = asId("seapoint");
    protected static final String IMPORT_DEST_DOMAIN_ID = asId("blackrock");

    protected A getResource(Executor executor,
                            PowerShellPoolMap poolMap,
                            PowerShellParser parser,
                            UriInfoProvider uriProvider) {
        return getResource(new PowerShellStorageDomainResource(STORAGE_DOMAIN_ID, new PowerShellStorageDomainsResource(), poolMap, parser), poolMap, parser);
    }

    protected abstract A getResource(PowerShellStorageDomainResource parent,
                                     PowerShellPoolMap poolMap,
                                     PowerShellParser parser);

    @Override
    protected void setUriInfo(UriInfo uriInfo) {
        super.setUriInfo(uriInfo);
        resource.setUriInfo(uriInfo);
    }

    protected void setUpCmdExpectations(String command, String ret) {
        mockStatic(PowerShellCmd.class);
        expect(PowerShellCmd.runCommand(setUpPoolExpectations(), command)).andReturn(ret);
    }
}

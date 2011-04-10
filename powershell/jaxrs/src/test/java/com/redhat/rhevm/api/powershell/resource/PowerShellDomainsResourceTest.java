/*
 * Copyright © 2011 Red Hat, Inc.
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

import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.model.Domains;
import com.redhat.rhevm.api.model.Domain;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;

import org.junit.Test;

import static org.easymock.classextension.EasyMock.expect;

import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;

public class PowerShellDomainsResourceTest extends AbstractPowerShellSimpleResourceTest<Domain, PowerShellDomainsResource> {

    private static final String DOMAIN_NAME = "blaa.waterford.com";
    private static final String SELECT_COMMAND = "foreach ($u in select-user) { $u.domain }";

    protected PowerShellDomainsResource getResource(Executor executor, PowerShellPoolMap poolMap, PowerShellParser parser, UriInfoProvider uriProvider) {
        PowerShellDomainsResource resource = new PowerShellDomainsResource();
        resource.setExecutor(executor);
        resource.setPowerShellPoolMap(poolMap);
        resource.setParser(parser);
        return resource;
    }

    @Test
    public void testList() throws Exception {
        setUpDomainExpectations(SELECT_COMMAND, formatDomain(DOMAIN_NAME));
        resource.setUriInfo(setUpBasicUriExpectations());
        replayAll();
        verifyDomains(resource.list());
    }

    @Test
    public void testGet() throws Exception {
        PowerShellDomainResource subresource = new PowerShellDomainResource(resource, "blaa.waterford.com");
        resource.setUriInfo(setUpBasicUriExpectations());
        replayAll();
        verifyDomain(subresource.get());
    }

    protected void verifyDomains(Domains domains) {
        assertNotNull(domains);
        assertEquals(1, domains.getDomains().size());
        verifyDomain(domains.getDomains().get(0));
    }

    static void verifyDomain(Domain domain) {
        assertNotNull(domain);
        assertEquals(DOMAIN_NAME, domain.getId());
        assertEquals(DOMAIN_NAME, domain.getName());
        verifyLinks(domain);
    }

    protected String formatDomain(String name) {
        return formatXmlReturn("domain",
                               new String[] { name },
                               new String[] { "" });
    }

    protected void setUpDomainExpectations(String command, String ret) throws Exception {
        mockStatic(PowerShellCmd.class);
        expect(PowerShellCmd.runCommand(setUpPoolExpectations(), command)).andReturn(ret);
    }
}

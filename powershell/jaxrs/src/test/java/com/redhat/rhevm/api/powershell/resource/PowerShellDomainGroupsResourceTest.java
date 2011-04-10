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
import com.redhat.rhevm.api.model.Group;
import com.redhat.rhevm.api.model.Groups;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;

import org.junit.Test;

import static org.easymock.classextension.EasyMock.expect;

import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;

public class PowerShellDomainGroupsResourceTest extends AbstractPowerShellSimpleResourceTest<Group, PowerShellDomainGroupsResource> {

    private static final String DOMAIN_NAME = "blaa.waterford.com";
    private static final String GROUP_NAME = "administrators";
    private static final String GROUP_ID = Integer.toString(GROUP_NAME.hashCode());

    private static final String[] FORMAT_ARGS = { DOMAIN_NAME };

    private static final String LIST_COMMAND = "select-user -ad -domain \"" + DOMAIN_NAME + "\" | ? { $_.isgroup() }";
    private static final String GET_COMMAND = "select-user -ad -domain \"" + DOMAIN_NAME + "\" | ? { $_.userid -eq \"" + GROUP_ID + "\" }";

    protected PowerShellDomainGroupsResource getResource(Executor executor, PowerShellPoolMap poolMap, PowerShellParser parser, UriInfoProvider uriProvider) {
        return new PowerShellDomainGroupsResource(new PowerShellDomainResource(new PowerShellDomainsResource(), DOMAIN_NAME),
                                                  executor, poolMap, parser);
    }

    @Test
    public void testList() throws Exception {
        setUpGroupExpectations(LIST_COMMAND, formatGroup(GROUP_NAME));
        resource.setUriInfo(setUpBasicUriExpectations());
        replayAll();
        verifyGroups(resource.list());
    }

    @Test
    public void testGet() throws Exception {
        PowerShellDomainGroupResource subresource = new PowerShellDomainGroupResource(resource, GROUP_ID);
        setUpGroupExpectations(GET_COMMAND, formatGroup(GROUP_NAME));
        resource.setUriInfo(setUpBasicUriExpectations());
        replayAll();
        verifyGroup(subresource.get());
    }

    protected void verifyGroups(Groups groups) {
        assertNotNull(groups);
        assertEquals(1, groups.getGroups().size());
        verifyGroup(groups.getGroups().get(0));
    }

    static void verifyGroup(Group group) {
        assertNotNull(group);
        assertEquals(Integer.toString(GROUP_NAME.hashCode()), group.getId());
        assertEquals(GROUP_NAME + "@" + DOMAIN_NAME, group.getName());
        verifyLinks(group);
        assertNotNull(group.getDomain());
        assertEquals(DOMAIN_NAME, group.getDomain().getId());
        verifyLinks(group.getDomain());
    }

    protected String formatGroup(String name) {
        return formatXmlReturn("group",
                               new String[] { name },
                               new String[] { "" },
                               FORMAT_ARGS);
    }

    protected void setUpGroupExpectations(String command, String ret) throws Exception {
        mockStatic(PowerShellCmd.class);
        expect(PowerShellCmd.runCommand(setUpPoolExpectations(), command)).andReturn(ret);
    }
}

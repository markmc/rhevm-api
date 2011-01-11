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
import com.redhat.rhevm.api.model.Creation;
import com.redhat.rhevm.api.model.Status;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellTestUtils;

import org.junit.Test;

import static org.easymock.EasyMock.expect;

import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;

public class PowerShellCreationResourceTest extends AbstractPowerShellResourceTest<Creation, PowerShellCreationResource> {

    private static final String TASK_IDS = "9d29775d-b685-411f-b304-07acc2fb7528,0b9318b4-e426-4380-9e6a-bb7f3a38a2ce";

    private static final String GET_COMMAND = "get-tasksstatus -commandtasksids " + TASK_IDS;

    protected PowerShellCreationResource getResource(Executor executor, PowerShellPoolMap poolMap, PowerShellParser parser, UriInfoProvider uriProvider) {
        return new PowerShellCreationResource(TASK_IDS, executor, uriProvider, poolMap, parser);
    }

    @Test
    public void testGet() throws Exception {
        setUriInfo(setUpCreationExpectations(GET_COMMAND,
                                             PowerShellTestUtils.readClassPathFile("tasks.xml")));
        verifyCreation(resource.get());
    }

    private UriInfo setUpCreationExpectations(String command, String ret) throws Exception {
        mockStatic(PowerShellCmd.class);
        expect(PowerShellCmd.runCommand(setUpPoolExpectations(), command)).andReturn(ret);
        UriInfo uriInfo = setUpBasicUriExpectations();
        expect(uriInfo.getPath()).andReturn(URI_BASE).anyTimes();
        replayAll();
        return uriInfo;
    }

    private void verifyCreation(Creation creation) {
        assertNotNull(creation);
        assertEquals(Status.FAILED, creation.getStatus());
        verifyLinks(creation);
    }
}

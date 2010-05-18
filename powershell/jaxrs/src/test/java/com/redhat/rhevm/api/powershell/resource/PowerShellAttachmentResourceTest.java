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

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Attachment;
import com.redhat.rhevm.api.model.StorageDomainStatus;

import com.redhat.rhevm.api.powershell.util.PowerShellCmd;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.isA;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { PowerShellCmd.class })
public class PowerShellAttachmentResourceTest extends Assert {

    private static final String URI_ROOT = "http://localhost:8099";
    private static final String DATA_CENTER_ID = "d12345";
    private static final String STORAGE_DOMAIN_ID = "s98765";
    private static final String DATA_CENTER_URI = URI_ROOT + "/datacenters/" + DATA_CENTER_ID;
    private static final String STORAGE_DOMAIN_URI = URI_ROOT + "/storagedomains/" + STORAGE_DOMAIN_ID;
    private static final String ATTACHMENT_URI = STORAGE_DOMAIN_URI + "/attachments/" + DATA_CENTER_ID;
    private static final String DEACTIVATE_ACTION_URI = ATTACHMENT_URI + "/deactivate";
    private static final String GET_COMMAND =
        "get-storagedomain" + " -datacenterid " + DATA_CENTER_ID + " -storagedomainid " + STORAGE_DOMAIN_ID;
    private static final String GET_RETURN =
        "storagedomainid:" + STORAGE_DOMAIN_ID + "\n" +
        "status: active\n" +
        "sharedstatus: active\n" +
        "domaintype: data (master)\n" +
        "type: nfs\n" +
        "nfspath: foo.bar:/blaa/and/butter\n";

    private PowerShellAttachmentResource resource;

    @Before
    public void setUp() {
        resource = new PowerShellAttachmentResource(DATA_CENTER_ID, STORAGE_DOMAIN_ID);
    }

    @After
    public void tearDown() {
        verifyAll();
    }

    @Test
    public void testGet() throws Exception {
        verifyAttachment(resource.get(setUpAttachmentExpectations()));
    }

    private UriInfo setUpAttachmentExpectations() throws Exception {
        mockStatic(PowerShellCmd.class);
        expect(PowerShellCmd.runCommand(GET_COMMAND)).andReturn(GET_RETURN);
        UriInfo uriInfo = createMock(UriInfo.class);
        UriBuilder uriBuilder = createMock(UriBuilder.class);
        expect(uriInfo.getRequestUriBuilder()).andReturn(uriBuilder).anyTimes();
        expect(uriInfo.getBaseUriBuilder()).andReturn(uriBuilder).anyTimes();
        expect(uriBuilder.clone()).andReturn(uriBuilder).anyTimes();
        expect(uriBuilder.path(isA(String.class))).andReturn(uriBuilder).anyTimes();
        expect(uriBuilder.build()).andReturn(new URI(ATTACHMENT_URI));
        expect(uriBuilder.build()).andReturn(new URI(STORAGE_DOMAIN_URI));
        expect(uriBuilder.build()).andReturn(new URI(DATA_CENTER_URI));
        expect(uriBuilder.build()).andReturn(new URI(DEACTIVATE_ACTION_URI));
        replayAll();

        return uriInfo;
    }

    private void verifyAttachment(Attachment attachment) {
        assertNotNull(attachment);
        assertEquals(attachment.getId(), DATA_CENTER_ID);
        assertEquals(attachment.getName(), DATA_CENTER_ID);
        assertNotNull(attachment.getDataCenter());
        assertEquals(attachment.getDataCenter().getId(), DATA_CENTER_ID);
        assertNotNull(attachment.getStorageDomain());
        assertEquals(attachment.getStorageDomain().getId(), STORAGE_DOMAIN_ID);
        assertEquals(attachment.getStatus(), StorageDomainStatus.ACTIVE);
        assert(attachment.isMaster());
    }
}

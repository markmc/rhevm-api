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

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.Status;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;

import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;

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
@Ignore
public class BasePowerShellResourceTest extends Assert {
    protected static final String URI_ROOT = "http://localhost:8099";

    protected ControllableExecutor executor = new ControllableExecutor();

    @After
    public void tearDown() {
        verifyAll();
    }

    protected Action getAction() {
        return getAction(false);
    }

    protected Action getAction(boolean async) {
        Action action = new Action();
        action.setId("56789");
        action.setAsync(async);
        return action;
    }

    protected UriInfo setUpActionExpectation(String baseUri, String verb, String command, String ret) throws Exception {
        mockStatic(PowerShellCmd.class);
        expect(PowerShellCmd.runCommand(command)).andReturn(ret);

        URI replayUri = new URI(baseUri + verb);
        URI actionUri = new URI(baseUri + verb + "/56789");

        UriInfo uriInfo = createMock(UriInfo.class);
        UriBuilder uriBuilder = createMock(UriBuilder.class);
        expect(uriInfo.getRequestUriBuilder()).andReturn(uriBuilder);
        expect(uriBuilder.path(isA(String.class))).andReturn(uriBuilder);
        expect(uriBuilder.build()).andReturn(actionUri);
        expect(uriInfo.getRequestUri()).andReturn(replayUri);

        replayAll();

        return uriInfo;
    }

    protected void verifyActionResponse(Response r, String baseUri, boolean async) throws Exception {
        assertEquals("unexpected status", async ? 202 : 200, r.getStatus());
        Object entity = r.getEntity();
        assertTrue("expect Action response entity", entity instanceof Action);
        Action action = (Action)entity;
        assertNotNull(action.getHref());
        assertNotNull(action.getId());
        assertNotNull(action.getLink());
        assertEquals(async, action.isAsync());
        assertTrue("unexpected status", async
                   ? action.getStatus().equals(Status.PENDING)
                     || action.getStatus().equals(Status.IN_PROGRESS)
                     || action.getStatus().equals(Status.COMPLETE)
                   : action.getStatus().equals(Status.COMPLETE));
        assertEquals(1, action.getLink().size());
        assertEquals("expected replay link", "replay", action.getLink().get(0).getRel());
        assertNotNull(action.getLink().get(0).getHref());
        assertTrue(action.getLink().get(0).getHref().startsWith(baseUri));
        assertEquals("unexpected async task", async ? 1 : 0, executor.taskCount());
        executor.runNext();
    }

}

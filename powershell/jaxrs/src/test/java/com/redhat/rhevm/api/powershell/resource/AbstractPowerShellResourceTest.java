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

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.BaseResource;
import com.redhat.rhevm.api.model.Status;
import com.redhat.rhevm.api.common.resource.AbstractActionableResource;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;

import org.junit.runner.RunWith;

import static org.easymock.classextension.EasyMock.expect;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { PowerShellCmd.class })
@Ignore
public abstract class AbstractPowerShellResourceTest<R extends BaseResource,
                                                     A extends AbstractActionableResource<R>>
    extends Assert {

    protected static final String URI_ROOT = "http://localhost:8099";

    protected A resource;
    protected ControllableExecutor executor;

    @Before
    public void setUp() {
        executor = new ControllableExecutor();
        resource = getResource();
        resource.setExecutor(executor);
    }

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

    protected UriInfo setUpActionExpectation(String baseUri, String verb, String command, Object ret) throws Exception {
        mockStatic(PowerShellCmd.class);
        if (ret instanceof Throwable) {
            expect(PowerShellCmd.runCommand(command)).andThrow((Throwable)ret);
        } else  {
            expect(PowerShellCmd.runCommand(command)).andReturn((String)ret);
        }

        UriInfo uriInfo = createMock(UriInfo.class);
        expect(uriInfo.getPath()).andReturn(baseUri).times(2);

        replayAll();

        return uriInfo;
    }


    protected void verifyActionResponse(Response r, String baseUri, boolean async) throws Exception {
        verifyActionResponse(r, baseUri, async, null, null);
    }

    protected void verifyActionResponse(Response r, String baseUri, boolean async, String reason, String detailExerpt) throws Exception {
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
                   : reason == null
                     ? action.getStatus().equals(Status.COMPLETE)
                     : action.getStatus().equals(Status.FAILED));
        // FIXME: https://fedorahosted.org/rhevm-api/ticket/29
        assertTrue(action.getLink().size() == 1 || action.getLink().size() == 2);
        int i = 0;
        if (action.getLink().size() == 2) {
            assertEquals("expected parent link", "parent", action.getLink().get(i).getRel());
            assertNotNull(action.getLink().get(i).getHref());
            assertTrue(action.getLink().get(i).getHref().startsWith(baseUri));
            ++i;
        }
        assertEquals("expected replay link", "replay", action.getLink().get(i).getRel());
        assertNotNull(action.getLink().get(i).getHref());
        assertTrue(action.getLink().get(i).getHref().startsWith(baseUri));
        assertEquals("unexpected async task", async ? 1 : 0, executor.taskCount());
        executor.runNext();
        if (reason != null) {
            assertNotNull(action.getFault());
            assertEquals(reason, action.getFault().getReason());
            assertTrue(action.getFault().getDetail().indexOf(detailExerpt) != -1);
        }
    }

    protected abstract A getResource();
}

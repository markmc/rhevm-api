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
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Fault;
import com.redhat.rhevm.api.model.Host;

import com.redhat.rhevm.api.powershell.util.PowerShellCmd;

import org.junit.Test;

import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.isA;

import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;


public class PowerShellHostResourceTest extends AbstractPowerShellResourceTest<Host, PowerShellHostResource> {

    private static final String GET_COMMAND = "rhevmpssnapin\\get-host 12345";
    private static final String GET_RETURN = "hostid: 12345 \n name: sedna";
    private static final String ACTION_RETURN = "replace with realistic powershell return";
    private static final String UPDATE_COMMAND = "$h = get-host 12345\n$h.name = 'eris'\nupdate-host -hostobject $h";
    private static final String UPDATE_RETURN = "hostid: 12345 \n name: eris";

    protected PowerShellHostResource getResource() {
        return new PowerShellHostResource("12345", executor);
    }

    @Test
    public void testGet() throws Exception {
        verifyHost(
            resource.get(setUpHostExpectations(GET_COMMAND, GET_RETURN, "sedna")),
            "sedna");
    }

    @Test
    public void testGoodUpdate() throws Exception {
        verifyHost(
            resource.update(createMock(HttpHeaders.class),
                            setUpHostExpectations(UPDATE_COMMAND, UPDATE_RETURN, "eris"),
                            getHost("eris")),
            "eris");
    }

    @Test
    public void testBadUpdate() throws Exception {
        try {
            UriInfo uriInfo = createMock(UriInfo.class);
            resource.update(setUpHeadersExpectation(),
                            uriInfo,
                            getHost("98765", "eris"));
            fail("expected WebApplicationException on bad update");
        } catch (WebApplicationException wae) {
            verifyUpdateException(wae);
        }
    }

    @Test
    public void testApprove() throws Exception {
        verifyActionResponse(
            resource.approve(setUpActionExpectation("approve", "approve-host"), getAction()),
            false);
    }

    @Test
    public void testFence() throws Exception {
        verifyActionResponse(
            resource.fence(setUpActionExpectation("fence", "fence-host"), getAction()),
            false);
    }

    @Test
    public void testActivate() throws Exception {
        verifyActionResponse(
            resource.activate(setUpActionExpectation("activate", "resume-host"), getAction()),
            false);
    }

    @Test
    public void testDeactivate() throws Exception {
        verifyActionResponse(
            resource.deactivate(setUpActionExpectation("deactivate", "suspend-host"), getAction()),
            false);
    }

    @Test
    public void testApproveAsync() throws Exception {
        verifyActionResponse(
            resource.approve(setUpActionExpectation("approve", "approve-host"), getAction(true)),
            true);
    }

    @Test
    public void testFenceAsync() throws Exception {
        verifyActionResponse(
            resource.fence(setUpActionExpectation("fence", "fence-host"), getAction(true)),
            true);
    }

    @Test
    public void testActivateAsync() throws Exception {
        verifyActionResponse(
            resource.activate(setUpActionExpectation("activate", "resume-host"), getAction(true)),
            true);
    }

    @Test
    public void testDeactivateAsync() throws Exception {
        verifyActionResponse(
            resource.deactivate(setUpActionExpectation("deactivate", "suspend-host"), getAction(true)),
            true);
    }

    private UriInfo setUpHostExpectations(String command, String ret, String name) throws Exception {
        mockStatic(PowerShellCmd.class);
        expect(PowerShellCmd.runCommand(command)).andReturn(ret);
        String href = URI_ROOT + "/hosts/12345";
        UriInfo uriInfo = createMock(UriInfo.class);
        UriBuilder uriBuilder = createMock(UriBuilder.class);
        expect(uriInfo.getRequestUriBuilder()).andReturn(uriBuilder).anyTimes();
        expect(uriBuilder.build()).andReturn(new URI(href)).anyTimes();
        UriBuilder actionUriBuilder = createMock(UriBuilder.class);
        expect(uriBuilder.clone()).andReturn(actionUriBuilder).anyTimes();
        expect(actionUriBuilder.path(isA(String.class))).andReturn(uriBuilder).anyTimes();
        expect(actionUriBuilder.build()).andReturn(new URI(href + "/action")).anyTimes();
        replayAll();

        return uriInfo;
    }

    private UriInfo setUpActionExpectation(String verb, String command) throws Exception {
        return setUpActionExpectation("/hosts/12345/", verb, command + " -hostid 12345", ACTION_RETURN);
    }

    private HttpHeaders setUpHeadersExpectation() {
        HttpHeaders headers = createMock(HttpHeaders.class);
        List<MediaType> mediaTypes = new ArrayList<MediaType>();
        mediaTypes.add(MediaType.APPLICATION_XML_TYPE);
        expect(headers.getAcceptableMediaTypes()).andReturn(mediaTypes).anyTimes();
        replayAll();
        return headers;
    }

    private Host getHost(String name) {
        return getHost("12345", name);
    }

    private Host getHost(String id, String name) {
        Host host = new Host();
        host.setId(id);
        host.setName(name);
        return host;
    }

    private void verifyHost(Host host, String name) {
        assertNotNull(host);
        assertEquals(host.getId(), "12345");
        assertEquals(host.getName(), name);
    }

    private void verifyActionResponse(Response r, boolean async) throws Exception {
        verifyActionResponse(r, "/hosts/12345/", async);
    }

    private void verifyUpdateException(WebApplicationException wae) {
        assertEquals(409, wae.getResponse().getStatus());
        Fault fault = (Fault)wae.getResponse().getEntity();
        assertNotNull(fault);
        assertEquals("Broken immutability constraint", fault.getReason());
        assertEquals("Attempt to set immutable field: id", fault.getDetail());
    }
}

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
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.Fault;
import com.redhat.rhevm.api.model.Status;
import com.redhat.rhevm.api.model.VM;

import com.redhat.rhevm.api.powershell.util.PowerShellUtils;

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
@PrepareForTest( { PowerShellUtils.class })
public class PowerShellVmResourceTest extends Assert {

    private static final String URI_ROOT = "http://localhost:8099";
    private static final String GET_RETURN = "vmid: 12345 \n name: sedna";
    private static final String ACTION_RETURN = "replace with realistic powershell return";
    private static final String UPDATE_COMMAND = "$v = get-vm 12345\n$v.name = \"eris\"\nupdate-vm -vmobject $v";
    private static final String UPDATE_RETURN = "vmid: 12345 \n name: eris";

    private PowerShellVmResource resource;

    @Before
    public void setUp() {
        resource = new PowerShellVmResource("12345");
    }

    @After
    public void tearDown() {
        verifyAll();
    }

    @Test
    public void testGet() throws Exception {
        verifyVM(
            resource.get(setUpVmExpectations("get-vm 12345", GET_RETURN, "sedna")),
            "sedna");
    }

    @Test
    public void testGoodUpdate() throws Exception {
        verifyVM(
            resource.update(createMock(HttpHeaders.class),
                            setUpVmExpectations(UPDATE_COMMAND, UPDATE_RETURN, "eris"),
                            getVM("eris")),
            "eris");
    }

    @Test
    public void testBadUpdate() throws Exception {
        try {
            UriInfo uriInfo = createMock(UriInfo.class);
            resource.update(setUpHeadersExpectation(),
                            uriInfo,
                            getVM("98765", "eris"));
            fail("expected WebApplicationException on bad update");
        } catch (WebApplicationException wae) {
            verifyUpdateException(wae);
        }
    }

    @Test
    public void testStart() throws Exception {
        verifyResponse(
            resource.start(setUpActionExpectation("start", "start-vm"), getAction()),
            false);
    }

    @Test
    public void testStop() throws Exception {
        verifyResponse(
            resource.stop(setUpActionExpectation("stop", "stop-vm"), getAction()),
            false);
    }

    @Test
    public void testShutdown() throws Exception {
        verifyResponse(
            resource.shutdown(setUpActionExpectation("shutdown", "shutdown-vm"), getAction()),
            false);
    }

    @Test
    public void testSuspend() throws Exception {
        verifyResponse(
            resource.suspend(setUpActionExpectation("suspend", "suspend-vm"), getAction()),
            false);
    }

    @Test
    public void testRestore() throws Exception {
        verifyResponse(
            resource.restore(setUpActionExpectation("restore", "restore-vm"), getAction()),
            false);
    }

    @Test
    public void testStartAsync() throws Exception {
        verifyResponse(
            resource.start(setUpActionExpectation("start", "start-vm"), getAction(true)),
            true);
    }

    @Test
    public void testStopAsync() throws Exception {
        verifyResponse(
            resource.stop(setUpActionExpectation("stop", "stop-vm"), getAction(true)),
            true);
    }

    @Test
    public void testShutdownAsync() throws Exception {
        verifyResponse(
            resource.shutdown(setUpActionExpectation("shutdown", "shutdown-vm"), getAction(true)),
            true);
    }

    @Test
    public void testSuspendAsync() throws Exception {
        verifyResponse(
            resource.suspend(setUpActionExpectation("suspend", "suspend-vm"), getAction(true)),
            true);
    }

    @Test
    public void testRestoreAsync() throws Exception {
        verifyResponse(
            resource.restore(setUpActionExpectation("restore", "restore-vm"), getAction(true)),
            true);
    }

    private UriInfo setUpVmExpectations(String command, String ret, String name) throws Exception {
        mockStatic(PowerShellUtils.class);
        expect(PowerShellUtils.runCommand(command)).andReturn(ret);
        expect(PowerShellUtils.parseProps(ret)).andReturn(getProps(name));
        UriInfo uriInfo = createMock(UriInfo.class);
        UriBuilder uriBuilder = createMock(UriBuilder.class);
        expect(uriInfo.getRequestUriBuilder()).andReturn(uriBuilder).anyTimes();
        expect(uriBuilder.build()).andReturn(new URI(URI_ROOT + "/vms/12345")).anyTimes();
        replayAll();

        return uriInfo;
    }

    private UriInfo setUpActionExpectation(String verb, String command) throws Exception {
        mockStatic(PowerShellUtils.class);
        expect(PowerShellUtils.runCommand(command + " -vmid 12345")).andReturn(ACTION_RETURN);

        URI replayUri = new URI("/vms/12345/" + verb);
        URI actionUri = new URI("/vms/12345/" + verb + "/56789");

        UriInfo uriInfo = createMock(UriInfo.class);
        UriBuilder uriBuilder = createMock(UriBuilder.class);
        expect(uriInfo.getRequestUriBuilder()).andReturn(uriBuilder);
        expect(uriBuilder.path(isA(String.class))).andReturn(uriBuilder);
        expect(uriBuilder.build()).andReturn(actionUri);
        expect(uriInfo.getRequestUri()).andReturn(replayUri);

        replayAll();

        return uriInfo;
    }

    private ArrayList<HashMap<String,String>> getProps(String name) {
        ArrayList<HashMap<String,String>> parsedProps = new ArrayList<HashMap<String,String>>();
        HashMap<String,String> vmProps = new HashMap<String,String>();
        vmProps.put("vmid", "12345");
        vmProps.put("name", name);
        parsedProps.add(vmProps);
        return parsedProps;
    }

    private HttpHeaders setUpHeadersExpectation() {
        HttpHeaders headers = createMock(HttpHeaders.class);
        List<MediaType> mediaTypes = new ArrayList<MediaType>();
        mediaTypes.add(MediaType.APPLICATION_XML_TYPE);
        expect(headers.getAcceptableMediaTypes()).andReturn(mediaTypes).anyTimes();
        replayAll();
        return headers;
    }

    private VM getVM(String name) {
        return getVM("12345", name);
    }

    private VM getVM(String id, String name) {
        VM vm = new VM();
        vm.setId(id);
        vm.setName(name);
        return vm;
    }

    private Action getAction() {
        return getAction(false);
    }

    private Action getAction(boolean async) {
        Action action = new Action();
        action.setId("56789");
        action.setAsync(async);
        return action;
    }

    private void verifyVM(VM vm, String name) {
        assertNotNull(vm);
        assertEquals(vm.getId(), "12345");
        assertEquals(vm.getName(), name);
    }

    private void verifyResponse(Response r, boolean async) throws Exception {
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
        assertTrue(action.getLink().get(0).getHref().startsWith("/vms/12345/"));
        Thread.sleep(200);
    }

    private void verifyUpdateException(WebApplicationException wae) {
        assertEquals(409, wae.getResponse().getStatus());
        Fault fault = (Fault)wae.getResponse().getEntity();
        assertNotNull(fault);
        assertEquals("Broken immutability constraint", fault.getReason());
        assertEquals("Attempt to set immutable field: id", fault.getDetail());
    }
}

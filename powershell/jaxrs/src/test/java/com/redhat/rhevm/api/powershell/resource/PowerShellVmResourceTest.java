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
import com.redhat.rhevm.api.model.VM;

import com.redhat.rhevm.api.powershell.util.PowerShellCmd;

import org.junit.Test;

import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.isA;

import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;


public class PowerShellVmResourceTest extends AbstractPowerShellResourceTest<VM, PowerShellVmResource> {

    private static final String VM_ID = "12345";
    private static final String VM_NAME = "sedna";
    private static final String NEW_NAME = "eris";
    private static final String CLUSTER_ID = "3321";
    private static final String BAD_ID = "98765";

    private static final String GET_RETURN = "vmid: " + VM_ID + "\nname: " + VM_NAME + "\nhostclusterid: " + CLUSTER_ID + "\n";
    private static final String ACTION_RETURN = "replace with realistic powershell return";
    private static final String UPDATE_COMMAND = "$v = get-vm " + VM_ID + "\n$v.name = \"" + NEW_NAME + "\"\nupdate-vm -vmobject $v";
    private static final String UPDATE_RETURN = "vmid: " + VM_ID + "\n name: " + NEW_NAME + "\nhostclusterid: " + CLUSTER_ID + "\n";

    protected PowerShellVmResource getResource() {
        return new PowerShellVmResource(VM_ID);
    }

    @Test
    public void testGet() throws Exception {
        verifyVM(
            resource.get(setUpVmExpectations("get-vm " + VM_ID, GET_RETURN, VM_NAME)),
            VM_NAME);
    }

    @Test
    public void testGoodUpdate() throws Exception {
        verifyVM(
            resource.update(createMock(HttpHeaders.class),
                            setUpVmExpectations(UPDATE_COMMAND, UPDATE_RETURN, "eris"),
                            getVM(NEW_NAME)),
            NEW_NAME);
    }

    @Test
    public void testBadUpdate() throws Exception {
        try {
            UriInfo uriInfo = createMock(UriInfo.class);
            resource.update(setUpHeadersExpectation(),
                            uriInfo,
                            getVM(BAD_ID, NEW_NAME));
            fail("expected WebApplicationException on bad update");
        } catch (WebApplicationException wae) {
            verifyUpdateException(wae);
        }
    }

    @Test
    public void testStart() throws Exception {
        verifyActionResponse(
            resource.start(setUpActionExpectation("start", "start-vm"), getAction()),
            false);
    }

    @Test
    public void testStop() throws Exception {
        verifyActionResponse(
            resource.stop(setUpActionExpectation("stop", "stop-vm"), getAction()),
            false);
    }

    @Test
    public void testShutdown() throws Exception {
        verifyActionResponse(
            resource.shutdown(setUpActionExpectation("shutdown", "shutdown-vm"), getAction()),
            false);
    }

    @Test
    public void testSuspend() throws Exception {
        verifyActionResponse(
            resource.suspend(setUpActionExpectation("suspend", "suspend-vm"), getAction()),
            false);
    }

    @Test
    public void testRestore() throws Exception {
        verifyActionResponse(
            resource.restore(setUpActionExpectation("restore", "restore-vm"), getAction()),
            false);
    }

    @Test
    public void testStartAsync() throws Exception {
        verifyActionResponse(
            resource.start(setUpActionExpectation("start", "start-vm"), getAction(true)),
            true);
    }

    @Test
    public void testStopAsync() throws Exception {
        verifyActionResponse(
            resource.stop(setUpActionExpectation("stop", "stop-vm"), getAction(true)),
            true);
    }

    @Test
    public void testShutdownAsync() throws Exception {
        verifyActionResponse(
            resource.shutdown(setUpActionExpectation("shutdown", "shutdown-vm"), getAction(true)),
            true);
    }

    @Test
    public void testSuspendAsync() throws Exception {
        verifyActionResponse(
            resource.suspend(setUpActionExpectation("suspend", "suspend-vm"), getAction(true)),
            true);
    }

    @Test
    public void testRestoreAsync() throws Exception {
        verifyActionResponse(
            resource.restore(setUpActionExpectation("restore", "restore-vm"), getAction(true)),
            true);
    }

    private UriInfo setUpVmExpectations(String command, String ret, String name) throws Exception {
        mockStatic(PowerShellCmd.class);
        expect(PowerShellCmd.runCommand(command)).andReturn(ret);
        UriInfo uriInfo = createMock(UriInfo.class);
        UriBuilder uriBuilder = createMock(UriBuilder.class);
        expect(uriInfo.getRequestUriBuilder()).andReturn(uriBuilder).anyTimes();
        expect(uriBuilder.build()).andReturn(new URI(URI_ROOT + "/vms/" + VM_ID)).anyTimes();
        UriBuilder baseBuilder = createMock(UriBuilder.class);
        expect(uriInfo.getBaseUriBuilder()).andReturn(baseBuilder);
        expect(baseBuilder.clone()).andReturn(baseBuilder).anyTimes();
        expect(baseBuilder.path(isA(String.class))).andReturn(baseBuilder).anyTimes();
        expect(baseBuilder.build()).andReturn(new URI(URI_ROOT + "/foo")).anyTimes();
        replayAll();

        return uriInfo;
    }

    private UriInfo setUpActionExpectation(String verb, String command) throws Exception {
        return setUpActionExpectation("/vms/" + VM_ID + "/", verb, command + " -vmid " + VM_ID, ACTION_RETURN);
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
        return getVM(VM_ID, name);
    }

    private VM getVM(String id, String name) {
        VM vm = new VM();
        vm.setId(id);
        vm.setName(name);
        return vm;
    }

    private void verifyVM(VM vm, String name) {
        assertNotNull(vm);
        assertEquals(vm.getId(), VM_ID);
        assertEquals(vm.getName(), name);
    }

    private void verifyActionResponse(Response r, boolean async) throws Exception {
        verifyActionResponse(r, "/vms/" + VM_ID + "/", async);
    }

    private void verifyUpdateException(WebApplicationException wae) {
        assertEquals(409, wae.getResponse().getStatus());
        Fault fault = (Fault)wae.getResponse().getEntity();
        assertNotNull(fault);
        assertEquals("Broken immutability constraint", fault.getReason());
        assertEquals("Attempt to set immutable field: id", fault.getDetail());
    }
}

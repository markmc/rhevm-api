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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.Fault;
import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.model.VM;

import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellException;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;

import org.junit.Test;

import static org.easymock.classextension.EasyMock.expect;

import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;


public class PowerShellVmResourceTest extends AbstractPowerShellResourceTest<VM, PowerShellVmResource> {

    private static final String VM_ID = "12345";
    private static final String VM_NAME = "sedna";
    private static final String NEW_NAME = "eris";
    private static final String CLUSTER_ID = "3321";
    private static final String TEMPLATE_ID = "666";
    private static final String BAD_ID = "98765";

    private static final String OTHER_PROPS = "memorysize: 1024\ndefaultbootsequence: CDN\nnumofsockets: 2\nnumofcpuspersocket: 4\npoolid: -1\n";

    private static final String GET_RETURN = "vmid: " + VM_ID + "\nname: " + VM_NAME + "\nhostclusterid: " + CLUSTER_ID + "\n" + "templateid: " + TEMPLATE_ID + "\n" + OTHER_PROPS;
    private static final String ACTION_RETURN = "replace with realistic powershell return";
    private static final String FAILURE = "replace with realistic powershell failure";
    private static final String REASON = "Powershell command \"start-vm -vmid " + VM_ID + "\" failed with " + FAILURE;
    private static final String DETAIL = "at com.redhat.rhevm.api.powershell.util.PowerShellCmd.runCommand(";
    private static final String UPDATE_COMMAND = "$v = get-vm '" + VM_ID + "'\n$v.name = '" + NEW_NAME + "'\nupdate-vm -vmobject $v";
    private static final String UPDATE_RETURN = "vmid: " + VM_ID + "\n name: " + NEW_NAME + "\nhostclusterid: " + CLUSTER_ID + "\n" + "templateid: " + TEMPLATE_ID + "\n" + OTHER_PROPS;

    private static final String DEST_HOST_ID = "1337";
    private static final String DEST_HOST_NAME = "farawaysoclose";
    private static final String MIGRATE_COMMAND = "migrate-vm -vmid '" + VM_ID + "' -desthostid '" + DEST_HOST_ID + "'";
    private static final String MIGRATE_COMMAND_WITH_HOST_NAME =
        "$h = select-host -searchtext 'name=" + DEST_HOST_NAME + "'\n" +
        "migrate-vm -vmid '" + VM_ID + "' -desthostid $h.hostid";

    protected PowerShellVmResource getResource(Executor executor, PowerShellPoolMap poolMap) {
        return new PowerShellVmResource(VM_ID, executor, poolMap);
    }

    @Test
    public void testGet() throws Exception {
        verifyVM(
            resource.get(setUpVmExpectations("get-vm '" + VM_ID + "'", GET_RETURN, VM_NAME)),
            VM_NAME);
    }

    @Test
    public void testGoodUpdate() throws Exception {
        verifyVM(
            resource.update(setUpVmExpectations(UPDATE_COMMAND, UPDATE_RETURN, NEW_NAME),
                            getVM(NEW_NAME)),
            NEW_NAME);
    }

    @Test
    public void testBadUpdate() throws Exception {
        try {
            UriInfo uriInfo = createMock(UriInfo.class);
            replayAll();
            resource.update(uriInfo,
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
    public void testDetach() throws Exception {
        verifyActionResponse(
            resource.detach(setUpActionExpectation("detach", "detach-vm"), getAction()),
            false);
    }

    @Test
    public void testMigrate() throws Exception {
        Action action = getAction();
        action.setHost(new Host());
        action.getHost().setId(DEST_HOST_ID);
        verifyActionResponse(
            resource.migrate(setUpActionExpectation("migrate", MIGRATE_COMMAND, false, null), action),
            false);
    }

    @Test
    public void testMigrateWithHostName() throws Exception {
        Action action = getAction();
        action.setHost(new Host());
        action.getHost().setName(DEST_HOST_NAME);
        verifyActionResponse(
            resource.migrate(setUpActionExpectation("migrate", MIGRATE_COMMAND_WITH_HOST_NAME, false, null), action),
            false);
    }

    @Test
    public void testStartAsync() throws Exception {
        verifyActionResponse(
            resource.start(setUpActionExpectation("start", "start-vm"), getAction(true)),
            true);
    }

    @Test
    public void testStartAsyncFailed() throws Exception {
        verifyActionResponse(
            resource.start(setUpActionExpectation("start", "start-vm", new PowerShellException(FAILURE)), getAction(true)),
            true,
            REASON,
            DETAIL);
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
    public void testDetachAsync() throws Exception {
        verifyActionResponse(
            resource.detach(setUpActionExpectation("detach", "detach-vm"), getAction(true)),
            true);
    }

    @Test
    public void testMigrateAsync() throws Exception {
        Action action = getAction(true);
        action.setHost(new Host());
        action.getHost().setId(DEST_HOST_ID);
        verifyActionResponse(
            resource.migrate(setUpActionExpectation("migrate", MIGRATE_COMMAND, false, null), action),
            true);
    }

    @Test
    public void testMigrateAsyncWithHostName() throws Exception {
        Action action = getAction(true);
        action.setHost(new Host());
        action.getHost().setName(DEST_HOST_NAME);
        verifyActionResponse(
            resource.migrate(setUpActionExpectation("migrate", MIGRATE_COMMAND_WITH_HOST_NAME, false, null), action),
            true);
    }

    private UriInfo setUpVmExpectations(String command, String ret, String name) throws Exception {
        mockStatic(PowerShellCmd.class);
        expect(PowerShellCmd.runCommand(setUpShellExpectations(), command)).andReturn(ret);
        replayAll();
        return null;
    }

    private UriInfo setUpActionExpectation(String verb, String command, boolean appendVmId, Throwable t) throws Exception {
        if (appendVmId) {
            command += " -vmid '" + VM_ID + "'";
        }
        if (t == null) {
            return setUpActionExpectation("/vms/" + VM_ID + "/", verb, command, ACTION_RETURN);
        } else {
            return setUpActionExpectation("/vms/" + VM_ID + "/", verb, command, t);
        }
    }

    private UriInfo setUpActionExpectation(String verb, String command) throws Exception {
        return setUpActionExpectation(verb, command, true, null);
    }

    private UriInfo setUpActionExpectation(String verb, String command, Throwable t) throws Exception {
        return setUpActionExpectation(verb, command, true, t);
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
        verifyActionResponse(r, "vms/" + VM_ID, async);
    }

    private void verifyActionResponse(Response r, boolean async, String reason, String detailExerpt) throws Exception {
        verifyActionResponse(r, "vms/" + VM_ID, async, reason, detailExerpt);
    }

    private void verifyUpdateException(WebApplicationException wae) {
        assertEquals(409, wae.getResponse().getStatus());
        Fault fault = (Fault)wae.getResponse().getEntity();
        assertNotNull(fault);
        assertEquals("Broken immutability constraint", fault.getReason());
        assertEquals("Attempt to set immutable field: id", fault.getDetail());
    }

    protected static String[] asArray(String s) {
        return new String[] { s };
    }
}

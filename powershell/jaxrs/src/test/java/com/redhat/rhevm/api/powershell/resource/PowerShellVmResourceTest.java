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

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.concurrent.Executor;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.Display;
import com.redhat.rhevm.api.model.DisplayType;
import com.redhat.rhevm.api.model.Fault;
import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.model.Ticket;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.model.VmType;

import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellException;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;

import org.junit.Test;

import static org.easymock.classextension.EasyMock.expect;

import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;

import static com.redhat.rhevm.api.powershell.resource.PowerShellVmsResourceTest.GET_STATS;

public class PowerShellVmResourceTest extends AbstractPowerShellResourceTest<VM, PowerShellVmResource> {

    private static final String VM_NAME = "sedna";
    private static final String VM_ID = Integer.toString(VM_NAME.hashCode());
    private static final String NEW_NAME = "eris";
    private static final String CLUSTER_ID = PowerShellVmsResourceTest.CLUSTER_ID;
    private static final String TEMPLATE_ID = PowerShellVmsResourceTest.TEMPLATE_ID;
    private static final String BAD_ID = "98765";
    private static final String TICKET_VALUE = "flibbertigibbet";

    private static final String ACTION_RETURN = "replace with realistic powershell return";
    private static final String FAILURE = "replace with realistic powershell failure";
    private static final String REASON = "Powershell command \"start-vm -vmid \"" + VM_ID + "\"\" failed with " + FAILURE;
    private static final String DETAIL = "at com.redhat.rhevm.api.powershell.util.PowerShellCmd.runCommand(";
    private static final String UPDATE_COMMAND_TEMPLATE = "$v = get-vm \"" + VM_ID + "\";$v.name = \"" + NEW_NAME + "\";{0}update-vm -vmobject $v";
    private static final String UPDATE_COMMAND = MessageFormat.format(UPDATE_COMMAND_TEMPLATE, "");
    private static final String UPDATE_DISPLAY_COMMAND = MessageFormat.format(UPDATE_COMMAND_TEMPLATE, " $v.numofmonitors = 4; $v.displaytype = 'VNC';");
    private static final String TICKET_COMMAND = "set-vmticket -vmid \"" + VM_ID + "\" -ticket \"" + TICKET_VALUE + "\"";
    private static final String TICKET_EXPIRY_COMMAND = "set-vmticket -vmid \"" + VM_ID + "\" -validtime \"360\"";
    private static final String TICKET_RETURN = "<Objects><Object Type=\"RhevmCmd.CLIVmTicket\">"
        + "<Property Name=\"VmId\" Type=\"System.Guid\">" + VM_ID + "</Property>"
        + "<Property Name=\"Ticket\" Type=\"System.String\">" + TICKET_VALUE + "</Property>"
        + "<Property Name=\"ValidTime\" Type=\"System.Int32\">360</Property></Object></Objects>";

    private static final String DEST_HOST_ID = "1337";
    private static final String DEST_HOST_NAME = "farawaysoclose";
    private static final String MIGRATE_COMMAND = "migrate-vm -vmid \"" + VM_ID + "\" -desthostid \"" + DEST_HOST_ID + "\"";
    private static final String MIGRATE_COMMAND_WITH_HOST_NAME =
        "$h = select-host -searchtext \"name=" + DEST_HOST_NAME + "\";" +
        "migrate-vm -vmid \"" + VM_ID + "\" -desthostid $h.hostid";

    protected PowerShellVmResource getResource(Executor executor, PowerShellPoolMap poolMap, PowerShellParser parser, UriInfoProvider uriProvider) {
        return new PowerShellVmResource(VM_ID, executor, uriProvider, poolMap, parser);
    }

    protected String formatVm(String name) {
        return formatVm("vm", name);
    }

    protected String formatVm(String type, String name) {
        return formatXmlReturn(type,
                               new String[] { name },
                               new String[] { "" },
                               PowerShellVmsResourceTest.extraArgs);
    }

    @Test
    public void testGet() throws Exception {
        setUriInfo(setUpVmExpectations("get-vm \"" + VM_ID + "\"" + GET_STATS,
                                       formatVm(VM_NAME),
                                       VM_NAME));
        verifyVM(resource.get(), VM_NAME);
    }

    @Test
    public void testGet22() throws Exception {
        setUriInfo(setUpVmExpectations("get-vm \"" + VM_ID + "\"" + GET_STATS,
                                       formatVm("vm22", VM_NAME),
                                       VM_NAME));
        verifyVM(resource.get(), VM_NAME);
    }

    @Test
    public void testGoodUpdate() throws Exception {
        setUriInfo(setUpVmExpectations(UPDATE_COMMAND,
                                       formatVm(NEW_NAME),
                                       NEW_NAME));
        verifyVM(resource.update(getVM(NEW_NAME)), NEW_NAME);
    }

    @Test
    public void testUpdateDisplay() throws Exception {
        setUriInfo(setUpVmExpectations(UPDATE_DISPLAY_COMMAND,
                                       formatVm(NEW_NAME),
                                       NEW_NAME));
        verifyVM(resource.update(updateDisplay(getVM(NEW_NAME))), NEW_NAME);
    }

    @Test
    public void testBadIdUpdate() throws Exception {
        VM update = getVM(BAD_ID, NEW_NAME);
        doTestBadUpdate(update, "id");
    }

    @Test
    public void testBadTypeUpdate() throws Exception {
        VM update = getVM(NEW_NAME);
        update.setType(VmType.DESKTOP);
        doTestBadUpdate(update, "type");
    }

    public void doTestBadUpdate(VM update, String field) throws Exception {
        try {
            setUriInfo(createMock(UriInfo.class));
            replayAll();
            resource.update(update);
            fail("expected WebApplicationException on bad update");
        } catch (WebApplicationException wae) {
            verifyUpdateException(wae, field);
        }
    }

    @Test
    public void testStart() throws Exception {
        setUriInfo(setUpActionExpectation("start", "start-vm"));
        verifyActionResponse(resource.start(getAction()), false);
    }

    @Test
    public void testStop() throws Exception {
        setUriInfo(setUpActionExpectation("stop", "stop-vm"));
        verifyActionResponse(resource.stop(getAction()), false);
    }

    @Test
    public void testShutdown() throws Exception {
        setUriInfo(setUpActionExpectation("shutdown", "shutdown-vm"));
        verifyActionResponse(resource.shutdown(getAction()), false);
    }

    @Test
    public void testSuspend() throws Exception {
        setUriInfo(setUpActionExpectation("suspend", "suspend-vm"));
        verifyActionResponse(resource.suspend(getAction()), false);
    }

    @Test
    public void testDetach() throws Exception {
        setUriInfo(setUpActionExpectation("detach", "detach-vm"));
        verifyActionResponse(resource.detach(getAction()), false);
    }

    @Test
    public void testMigrate() throws Exception {
        Action action = getAction();
        action.setHost(new Host());
        action.getHost().setId(DEST_HOST_ID);
        setUriInfo(setUpActionExpectation("migrate", MIGRATE_COMMAND, false, null));
        verifyActionResponse(resource.migrate(action), false);
    }

    @Test
    public void testMigrateWithHostName() throws Exception {
        Action action = getAction();
        action.setHost(new Host());
        action.getHost().setName(DEST_HOST_NAME);
        setUriInfo(setUpActionExpectation("migrate", MIGRATE_COMMAND_WITH_HOST_NAME, false, null));
        verifyActionResponse(resource.migrate(action), false);
    }

    @Test
    public void testIncompleteMigrate() throws Exception {
        setUriInfo(setUpActionExpectation(null, null, null, null));
        try {
            resource.migrate(getAction());
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "Action", "migrate", "host.id|name");
        }
    }

    @Test
    public void testTicket() throws Exception {
        Action action = getAction();
        action.setTicket(new Ticket());
        action.getTicket().setValue(TICKET_VALUE);
        setUriInfo(setUpActionExpectation("ticket", TICKET_COMMAND, TICKET_RETURN, false, null));
        Response response = resource.ticket(action);
        verifyActionResponse(response, false);
        verifyTicketResponse(response);
    }

    @Test
    public void testStartAsync() throws Exception {
        setUriInfo(setUpActionExpectation("start", "start-vm"));
        verifyActionResponse(resource.start(getAction(true)), true);
    }

    @Test
    public void testStartAsyncFailed() throws Exception {
        setUriInfo(setUpActionExpectation("start", "start-vm", new PowerShellException(FAILURE)));
        verifyActionResponse(resource.start(getAction(true)), true, REASON, DETAIL);
    }

    @Test
    public void testStopAsync() throws Exception {
        setUriInfo(setUpActionExpectation("stop", "stop-vm"));
        verifyActionResponse(resource.stop(getAction(true)), true);
    }

    @Test
    public void testShutdownAsync() throws Exception {
        setUriInfo(setUpActionExpectation("shutdown", "shutdown-vm"));
        verifyActionResponse(resource.shutdown(getAction(true)), true);
    }

    @Test
    public void testSuspendAsync() throws Exception {
        setUriInfo(setUpActionExpectation("suspend", "suspend-vm"));
        verifyActionResponse(resource.suspend(getAction(true)),true);
    }

    @Test
    public void testDetachAsync() throws Exception {
        setUriInfo(setUpActionExpectation("detach", "detach-vm"));
        verifyActionResponse(resource.detach(getAction(true)), true);
    }

    @Test
    public void testMigrateAsync() throws Exception {
        Action action = getAction(true);
        action.setHost(new Host());
        action.getHost().setId(DEST_HOST_ID);
        setUriInfo(setUpActionExpectation("migrate", MIGRATE_COMMAND, false, null));
        verifyActionResponse(resource.migrate(action), true);
    }

    @Test
    public void testMigrateAsyncWithHostName() throws Exception {
        Action action = getAction(true);
        action.setHost(new Host());
        action.getHost().setName(DEST_HOST_NAME);
        setUriInfo(setUpActionExpectation("migrate", MIGRATE_COMMAND_WITH_HOST_NAME, false, null));
        verifyActionResponse(resource.migrate(action), true);
    }

    @Test
    public void testTicketAsync() throws Exception {
        Action action = getAction(true);
        action.setTicket(new Ticket());
        action.getTicket().setExpiry(360L);
        setUriInfo(setUpActionExpectation("ticket", TICKET_EXPIRY_COMMAND, TICKET_RETURN, false, null));
        verifyActionResponse(resource.ticket(action), true);
    }

    private UriInfo setUpVmExpectations(String command, String ret, String name) throws Exception {
        mockStatic(PowerShellCmd.class);
        expect(PowerShellCmd.runCommand(setUpPoolExpectations(), command)).andReturn(ret);
        UriInfo uriInfo = setUpBasicUriExpectations();
        replayAll();
        return uriInfo;
    }

    private UriInfo setUpActionExpectation(String verb, String command, boolean appendVmId, Throwable t) throws Exception {
        return setUpActionExpectation(verb, command, ACTION_RETURN, appendVmId, t);
    }

    private UriInfo setUpActionExpectation(String verb, String command, String ret, boolean appendVmId, Throwable t) throws Exception {
        if (appendVmId) {
            command += " -vmid \"" + VM_ID + "\"";
        }
        if (t == null) {
            return setUpActionExpectation("/vms/" + VM_ID + "/", verb, command, ret);
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

    private VM updateDisplay(VM vm) {
        vm.setDisplay(new Display());
        vm.getDisplay().setType(DisplayType.VNC);
        vm.getDisplay().setMonitors(4);
        return vm;
    }

    private void verifyVM(VM vm, String name) {
        assertNotNull(vm);
        assertEquals(Integer.toString(name.hashCode()), vm.getId());
        assertEquals(name, vm.getName());
        assertTrue(vm.isSetDisplay());
        assertEquals(DisplayType.SPICE, vm.getDisplay().getType());
        assertEquals(Integer.valueOf(1), vm.getDisplay().getMonitors());
        assertTrue(vm.getDisplay().getPort() == null || vm.getDisplay().getPort() != -1);
        assertTrue(vm.isSetMemoryStatistics());
        assertEquals(Long.valueOf(1024L), vm.getMemoryStatistics().getSize());
        assertEquals(Long.valueOf(50L), vm.getMemoryStatistics().getUtilization());
        assertTrue(vm.isSetCpuStatistics());
        assertEquals(BigDecimal.valueOf(10L), vm.getCpuStatistics().getUser());
        assertEquals(BigDecimal.valueOf(20L), vm.getCpuStatistics().getSystem());
        assertEquals(BigDecimal.valueOf(30L), vm.getCpuStatistics().getIdle());
        assertEquals(BigDecimal.valueOf(40L), vm.getCpuStatistics().getLoad());
        verifyLinks(vm);
    }

    private void verifyActionResponse(Response r, boolean async) throws Exception {
        verifyActionResponse(r, "vms/" + VM_ID, async);
    }

    private void verifyActionResponse(Response r, boolean async, String reason, String detailExerpt) throws Exception {
        verifyActionResponse(r, "vms/" + VM_ID, async, reason, detailExerpt);
    }

    private void verifyTicketResponse(Response r) {
        Action action = (Action)r.getEntity();
        assertTrue(action.isSetTicket());
        assertEquals(TICKET_VALUE, action.getTicket().getValue());
    }

    private void verifyUpdateException(WebApplicationException wae, String field) {
        assertEquals(409, wae.getResponse().getStatus());
        Fault fault = (Fault)wae.getResponse().getEntity();
        assertNotNull(fault);
        assertEquals("Broken immutability constraint", fault.getReason());
        assertEquals("Attempt to set immutable field: " + field, fault.getDetail());
    }

    protected static String[] asArray(String s) {
        return new String[] { s };
    }
}

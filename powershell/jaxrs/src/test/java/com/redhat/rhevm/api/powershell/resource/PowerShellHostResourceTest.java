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
import java.util.concurrent.Executor;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.Fault;
import com.redhat.rhevm.api.model.FenceType;
import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.model.IscsiDetails;
import com.redhat.rhevm.api.model.PowerManagement;
import com.redhat.rhevm.api.model.PowerManagementOption;
import com.redhat.rhevm.api.model.PowerManagementOptions;
import com.redhat.rhevm.api.model.PowerManagementStatus;
import com.redhat.rhevm.api.model.Status;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellTestUtils;

import org.junit.Test;

import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.isA;

import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;

import static com.redhat.rhevm.api.powershell.resource.PowerShellHostsResource.PROCESS_HOSTS;
import static com.redhat.rhevm.api.powershell.resource.PowerShellHostsResource.PROCESS_HOSTS_STATS;

public class PowerShellHostResourceTest extends AbstractPowerShellResourceTest<Host, PowerShellHostResource> {

    private static final String HOST_NAME = "sedna";
    private static final String HOST_ID = Integer.toString(HOST_NAME.hashCode());

    private static final String GET_COMMAND = "get-host \"" + HOST_ID + "\"";
    private static final String ACTION_RETURN = "replace with realistic powershell return";
    private static final String UPDATE_COMMAND = "$h = get-host \"" + HOST_ID + "\";$h.name = \"eris\";update-host -hostobject $h";
    private static final String UPDATE_PM_COMMAND = "$h = get-host \"" + HOST_ID + "\";$h.name = \"eris\";$h.powermanagement.enabled = $false; $h.powermanagement.type = \"fenceme\"; $h.powermanagement.address = \"foo\"; $h.powermanagement.username = \"me\"; $h.powermanagement.password = \"mysecret\"; $h.powermanagement.secure = $true; $h.powermanagement.port = \"12345\"; $h.powermanagement.slot = \"54321\"; $h.powermanagement.options = \"secure=true,port=12345,slot=54321\"; update-host -hostobject $h";
    private static final String INSTALL_PASSWORD = "boldlygoingnowhere";
    private static final String INSTALL_COMMAND = "$h = get-host \"" + HOST_ID + "\";update-host -hostobject $h -install -rootpassword \"" + INSTALL_PASSWORD + "\"";
    private static final String COMMIT_NET_CONFIG_COMMAND = "$h = get-host \"" + HOST_ID + "\"; commit-configurationchanges -hostobject $h";

    private static String ISCSI_PORTAL = "192.168.1.6";
    private static String ISCSI_TARGET = "iqn.2009-08.com.mycorp:mysan.foobar";
    private static String ISCSI_DISCOVER_COMMAND = "$cnx = new-storageserverconnection -storagetype ISCSI -connection \"" + ISCSI_PORTAL + "\" -portal \"" + ISCSI_PORTAL + "\"; get-storageserversendtargets -hostid \"" + HOST_ID + "\" -storageserverconnectionobject $cnx";
    private static String ISCSI_LOGIN_COMMAND = "$cnx = new-storageserverconnection -storagetype ISCSI -connection \"" + ISCSI_PORTAL + "\" -portal \"" + ISCSI_PORTAL + "\" -iqn \"" + ISCSI_TARGET + "\"; connect-storagetohost -hostid \"" + HOST_ID + "\" -storageserverconnectionobject $cnx";
    private static String FENCE_STATUS_COMMAND = "get-powermanagementstatus -hostid \"" + HOST_ID + "\"";

    protected PowerShellHostResource getResource(Executor executor, PowerShellPoolMap poolMap, PowerShellParser parser, UriInfoProvider uriProvider) {
        return new PowerShellHostResource(HOST_ID, executor, uriProvider, poolMap, parser, httpHeaders);
    }

    protected String formatHost(String name) {
        String ret = formatXmlReturn("host",
                               new String[] { name },
                               new String[] { "" },
                               PowerShellHostsResourceTest.extraArgs);
        return ret;
    }

    protected String formatStorageConnection() {
        return PowerShellTestUtils.readClassPathFile("storagecnx.xml");
    }

    @Test
    public void testGet() throws Exception {
        setUpHttpHeaderNullExpectations("Accept");
        setUriInfo(setUpHostExpectations(GET_COMMAND + PROCESS_HOSTS,
                                         formatHost(HOST_NAME),
                                         HOST_NAME));
        verifyHost(resource.get(), HOST_NAME);
    }

    @Test
    public void testGetIncludeStatistics() throws Exception {
        setUpHttpHeaderExpectations("Accept", "application/xml; detail=statistics");
        setUriInfo(setUpHostExpectations(GET_COMMAND + PROCESS_HOSTS_STATS,
                                         formatHost(HOST_NAME),
                                         HOST_NAME));
        Host host = resource.get();
        assertTrue(host.isSetStatistics());
        verifyHost(host, HOST_NAME);
    }

    @Test
    public void testGoodUpdate() throws Exception {
        setUpHttpHeaderNullExpectations("Accept");
        setUriInfo(setUpHostExpectations(UPDATE_COMMAND + PROCESS_HOSTS,
                                         formatHost("eris"),
                                         "eris"));
        verifyHost(resource.update(getHost(HOST_ID, "eris")), "eris");
    }

    @Test
    public void testUpdateIncludeStatistics() throws Exception {
        setUpHttpHeaderExpectations("Accept", "application/xml; detail=statistics");
        setUriInfo(setUpHostExpectations(UPDATE_COMMAND + PROCESS_HOSTS_STATS,
                                         formatHost("eris"),
                                         "eris"));
        Host host = resource.update(getHost(HOST_ID, "eris"));
        assertTrue(host.isSetStatistics());
        verifyHost(host, "eris");
    }


    @Test
    public void testPowerManagementUpdate() throws Exception {
        setUpHttpHeaderNullExpectations("Accept");
        Host host = getHost(HOST_ID, "eris");

        host.setPowerManagement(new PowerManagement());
        host.getPowerManagement().setEnabled(false);
        host.getPowerManagement().setType("fenceme");
        host.getPowerManagement().setAddress("foo");
        host.getPowerManagement().setUsername("me");
        host.getPowerManagement().setPassword("mysecret");
        host.getPowerManagement().setOptions(new PowerManagementOptions());
        host.getPowerManagement().getOptions().getOptions().add(buildOption("secure", "true"));
        host.getPowerManagement().getOptions().getOptions().add(buildOption("port", "12345"));
        host.getPowerManagement().getOptions().getOptions().add(buildOption("slot", "54321"));

        setUriInfo(setUpHostExpectations(UPDATE_PM_COMMAND + PROCESS_HOSTS,
                                         formatHost("eris"),
                                         "eris"));

        verifyHost(resource.update(host), "eris");
    }

    private PowerManagementOption buildOption(String name, String value) {
        PowerManagementOption option = new PowerManagementOption();
        option.setName(name);
        option.setValue(value);
        return option;
    }

    @Test
    public void testBadUpdate() throws Exception {
        try {
            setUriInfo(createMock(UriInfo.class));
            replayAll();
            resource.update(getHost("98765", "eris"));
            fail("expected WebApplicationException on bad update");
        } catch (WebApplicationException wae) {
            verifyUpdateException(wae);
        }
    }

    @Test
    public void testApprove() throws Exception {
        setUriInfo(setUpActionExpectation("approve", "approve-host"));
        verifyActionResponse(resource.approve(getAction()), false);
    }

    @Test
    public void testInstall() throws Exception {
        Action action = getAction();
        action.setRootPassword(INSTALL_PASSWORD);
        setUriInfo(setUpActionExpectation("hosts/" + HOST_ID, "install", INSTALL_COMMAND, ACTION_RETURN));
        verifyActionResponse(resource.install(action), false);
    }

    @Test
    public void testIncompleteInstall() throws Exception {
        setUriInfo(setUpActionExpectation(null, null, null, null));
        try {
            resource.install(getAction());
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "Action", "install", "rootPassword");
        }
    }

    @Test
    public void testActivate() throws Exception {
        setUriInfo(setUpActionExpectation("activate", "resume-host"));
        verifyActionResponse(resource.activate(getAction()), false);
    }

    @Test
    public void testDeactivate() throws Exception {
        setUriInfo(setUpActionExpectation("deactivate", "suspend-host"));
        verifyActionResponse(resource.deactivate(getAction()), false);
    }

    @Test
    public void testCommitNetConfig() throws Exception {
        setUriInfo(setUpActionExpectation("/hosts/"+ HOST_ID + "/",
                                          "commitnetconfig",
                                          COMMIT_NET_CONFIG_COMMAND,
                                          ACTION_RETURN));
        verifyActionResponse(resource.commitNetConfig(getAction()), false);
    }

    @Test
    public void testIscsiDiscover() throws Exception {
        Action action = getAction();
        action.setIscsi(new IscsiDetails());
        action.getIscsi().setAddress(ISCSI_PORTAL);
        setUriInfo(setUpActionExpectation("/hosts/" + HOST_ID + "/",
                                          "iscsidiscover",
                                          ISCSI_DISCOVER_COMMAND,
                                          formatStorageConnection()));
        Response response = resource.iscsiDiscover(action);
        verifyActionResponse(response, false);
        verifyIscsiTargets(response);
    }

    @Test
    public void testFenceStatusSucceed() throws Exception {
        Action action = getAction();
        action.setFenceType(FenceType.STATUS);
        setUriInfo(setUpActionExpectation("/hosts/" + HOST_ID + "/",
                                          "fence",
                                          FENCE_STATUS_COMMAND,
                                          "<Objects><Object Type=\"System.String\">Test Succeeded, Host Status is: on</Object></Objects>"));
        Response response = resource.fence(action);
        Action actionResult = (Action)response.getEntity();
        assertNotNull(actionResult.getPowerManagement());
        assertEquals(actionResult.getPowerManagement().getStatus(), PowerManagementStatus.ON);
        verifyActionResponse(response, false);
    }

    @Test
    public void testFenceStatusFail() throws Exception {
        Action action = getAction();
        action.setFenceType(FenceType.STATUS);
        setUriInfo(setUpActionExpectation("/hosts/" + HOST_ID + "/",
                                          "fence",
                                          FENCE_STATUS_COMMAND,
                                          "<Objects><Object Type=\"System.String\">Test Failed, Host Status is: off. Host burned by a madman.</Object></Objects>"));
        Response response = resource.fence(action);
        Action actionResult = (Action)response.getEntity();
        assertEquals(actionResult.getStatus(), Status.FAILED);
        assertNotNull(actionResult.getFault());
        assertEquals(actionResult.getFault().getReason(),
                     "Powershell command \"get-powermanagementstatus -hostid \"109313413\"\" failed with Host burned by a madman.");
    }

    @Test
    public void testIscsiLogin() throws Exception {
        Action action = getAction();
        action.setIscsi(new IscsiDetails());
        action.getIscsi().setAddress(ISCSI_PORTAL);
        action.getIscsi().setTarget(ISCSI_TARGET);
        setUriInfo(setUpActionExpectation("/hosts/" + HOST_ID + "/",
                                          "iscsilogin",
                                          ISCSI_LOGIN_COMMAND,
                                          ACTION_RETURN));
        verifyActionResponse(resource.iscsiLogin(action), false);
    }

    @Test
    public void testApproveAsync() throws Exception {
        setUriInfo(setUpActionExpectation("approve", "approve-host"));
        verifyActionResponse(resource.approve(getAction(true)), true);
    }

    @Test
    public void testInstallAsync() throws Exception {
        Action action = getAction(true);
        action.setRootPassword(INSTALL_PASSWORD);
        setUriInfo(setUpActionExpectation("hosts/" + HOST_ID, "install", INSTALL_COMMAND, ACTION_RETURN));
        verifyActionResponse(resource.install(action), true);
    }

    @Test
    public void testCommitNetConfigAsync() throws Exception {
        setUriInfo(setUpActionExpectation("/hosts/"+ HOST_ID + "/",
                                          "commitnetconfig",
                                          COMMIT_NET_CONFIG_COMMAND,
                                          ACTION_RETURN));
        verifyActionResponse(resource.commitNetConfig(getAction(true)), true);
    }

    @Test
    public void testActivateAsync() throws Exception {
        setUriInfo(setUpActionExpectation("activate", "resume-host"));
        verifyActionResponse(resource.activate(getAction(true)), true);
    }

    @Test
    public void testDeactivateAsync() throws Exception {
        setUriInfo(setUpActionExpectation("deactivate", "suspend-host"));
        verifyActionResponse(resource.deactivate(getAction(true)), true);
    }

    private UriInfo setUpHostExpectations(String command, String ret, String name) throws Exception {
        mockStatic(PowerShellCmd.class);
        expect(PowerShellCmd.runCommand(setUpPoolExpectations(), command)).andReturn(ret);
        String href = "hosts/" + Integer.toString(name.hashCode());
        UriInfo uriInfo = setUpBasicUriExpectations();
        UriBuilder uriBuilder = createMock(UriBuilder.class);
        expect(uriInfo.getRequestUriBuilder()).andReturn(uriBuilder).anyTimes();
        expect(uriBuilder.build()).andReturn(new URI(href)).anyTimes();
        UriBuilder actionUriBuilder = createMock(UriBuilder.class);
        expect(uriBuilder.clone()).andReturn(actionUriBuilder).anyTimes();
        expect(actionUriBuilder.path(isA(String.class))).andReturn(uriBuilder).anyTimes();
        expect(actionUriBuilder.build()).andReturn(new URI(href + "/action")).anyTimes();
        //expect(uriInfo.getBaseUri()).andReturn(URI.create(URI_BASE)).anyTimes();
        replayAll();

        return uriInfo;
    }

    private UriInfo setUpActionExpectation(String verb, String command) throws Exception {
        return setUpActionExpectation("/hosts/"+ HOST_ID + "/",
                                      verb,
                                      command + " -hostid \"" + HOST_ID + "\"",
                                      ACTION_RETURN);
    }

    private Host getHost(String name) {
        return getHost(Integer.toString(name.hashCode()), name);
    }

    private Host getHost(String id, String name) {
        Host host = new Host();
        host.setId(id);
        host.setName(name);
        return host;
    }

    private void verifyHost(Host host, String name) {
        assertNotNull(host);
        assertEquals(Integer.toString(name.hashCode()), host.getId());
        assertEquals(name, host.getName());
        verifyLinks(host);
    }

    private void verifyIscsiTargets(Response response) {
        assertNotNull(response);
        Action action = (Action)response.getEntity();
        assertTrue(action.isSetIscsiTargets());
        assertEquals(1, action.getIscsiTargets().size());
        assertEquals(ISCSI_TARGET, action.getIscsiTargets().get(0));
    }

    private void verifyActionResponse(Response r, boolean async) throws Exception {
        verifyActionResponse(r, "hosts/" + HOST_ID, async);
    }

    private void verifyUpdateException(WebApplicationException wae) {
        assertEquals(409, wae.getResponse().getStatus());
        Fault fault = (Fault)wae.getResponse().getEntity();
        assertNotNull(fault);
        assertEquals("Broken immutability constraint", fault.getReason());
        assertEquals("Attempt to set immutable field: id", fault.getDetail());
    }
}

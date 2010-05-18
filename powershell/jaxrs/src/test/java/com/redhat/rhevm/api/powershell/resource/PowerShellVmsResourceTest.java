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
import java.util.concurrent.Executor;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.model.VMs;

import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
import com.redhat.rhevm.api.resource.VmResource;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.easymock.classextension.EasyMock.expect;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;


@RunWith(PowerMockRunner.class)
@PrepareForTest( { PowerShellUtils.class, PowerShellCmd.class })
public class PowerShellVmsResourceTest extends Assert {

    private static final String URI_ROOT = "http://localhost:8099";

    private static final String SELECT_COMMAND = "select-vm";
    private static final String SELECT_RETURN =
	"vmid: " + "sedna".hashCode() + " \n name: sedna\n\n" +
	"vmid: " + "eris".hashCode() + " \n name: eris\n\n" +
	"vmid: " + "orcus".hashCode() + " \n name: orcus";

    private static final String ADD_COMMAND =
	"$templ = get-template -templateid template_1\n" +
	"add-vm -name ceres -templateobject $templ -hostclusterid hostcluster_1";
    private static final String ADD_RETURN =
	"vmid: " + "ceres".hashCode() + " \n name: ceres\n\n";

    private static final String REMOVE_COMMAND = "remove-vm -vmid " + "eris".hashCode();

    private PowerShellVmsResource resource;
    private Executor executor;

    @Before
    public void setUp() {
	executor = new ControllableExecutor();
        resource = new PowerShellVmsResource();
        resource.setExecutor(executor);
    }

    @After
    public void tearDown() {
        verifyAll();
    }

    @Test
    public void testList() throws Exception {
        verifyVMs(
            resource.list(setUpVmsExpectations(SELECT_COMMAND, SELECT_RETURN, "sedna", "eris", "orcus")),
            "sedna", "eris", "orcus");
    }

    @Test
    public void testAdd() throws Exception {
        verifyVmResponse(
            resource.add(setUpVmsExpectations(ADD_COMMAND, ADD_RETURN, "ceres"), getVM("ceres")),
            "ceres");
    }

    @Test
    public void testRemove() throws Exception {
	setUpVmsExpectations(REMOVE_COMMAND, null);
        resource.remove(Integer.toString("eris".hashCode()));
    }

    @Test
    public void testGetSubResource() throws Exception {
        verifyVmResource(
            resource.getVmSubResource(setUpVmsExpectations(null, null), Integer.toString("ceres".hashCode())),
            "ceres");
    }

    private UriInfo setUpVmsExpectations(String command, String ret, String ... names) throws Exception {
        if (command != null) {
            mockStatic(PowerShellCmd.class);
            expect(PowerShellCmd.runCommand(command)).andReturn(ret);
        }
        if (ret != null) {
            mockStatic(PowerShellUtils.class);
            expect(PowerShellUtils.parseProps(ret)).andReturn(getProps(names));
        }
        UriInfo uriInfo = createMock(UriInfo.class);
        for (String name : names) {
            UriBuilder uriBuilder = createMock(UriBuilder.class);
            expect(uriInfo.getRequestUriBuilder()).andReturn(uriBuilder);
            expect(uriBuilder.path(Integer.toString(name.hashCode()))).andReturn(uriBuilder);
            expect(uriBuilder.build()).andReturn(new URI(URI_ROOT + "/vms/" + name.hashCode())).anyTimes();
        }
        replayAll();

        return uriInfo;
    }

    private ArrayList<HashMap<String,String>> getProps(String ... names) {
        ArrayList<HashMap<String,String>> parsedProps = new ArrayList<HashMap<String,String>>();
        for (String name : names) {
            HashMap<String,String> vmProps = new HashMap<String,String>();
            vmProps.put("vmid", Integer.toString(name.hashCode()));
            vmProps.put("name", name);
            parsedProps.add(vmProps);
        }
        return parsedProps;
    }

    private VM getVM(String name) {
        VM vm = new VM();
        vm.setId(Integer.toString(name.hashCode()));
        vm.setName(name);
        vm.setTemplateId("template_1");
        vm.setClusterId("hostcluster_1");
        return vm;
    }

    private void verifyVmResponse(Response r, String name) {
        assertEquals("unexpected status", 201, r.getStatus());
        Object entity = r.getEntity();
        assertTrue("expect VM response entity", entity instanceof VM);
        VM vm = (VM)entity;
        assertEquals(vm.getId(), Integer.toString(name.hashCode()));
        assertEquals(vm.getName(), name);
        assertNotNull(r.getMetadata().get("Location"));
        assertTrue("expected location header",
        	   r.getMetadata().get("Location").size() > 0);
        assertEquals("unexpected location header",
        	     URI_ROOT + "/vms/" + name.hashCode(),
        	     r.getMetadata().get("Location").get(0).toString());
    }

    private void verifyVMs(VMs vms, String ... names) {
        assertNotNull(vms);
        List<VM> vmList = vms.getVMs();
        assertEquals("unexpected number of VMs", vmList.size(), names.length);
        for (String name: names) {
            VM vm = vmList.remove(0);
            assertEquals(vm.getId(), Integer.toString(name.hashCode()));
            assertEquals(vm.getName(), name);
        }
    }

    private void verifyVmResource(VmResource resource, String name) {
	assertNotNull(resource);
	assertTrue("unexpected resource type", resource instanceof PowerShellVmResource);
	PowerShellVmResource vmResource = (PowerShellVmResource)resource;
	assertEquals(vmResource.getId(), Integer.toString(name.hashCode()));
	assertSame(vmResource.getExecutor(), executor);
    }

}


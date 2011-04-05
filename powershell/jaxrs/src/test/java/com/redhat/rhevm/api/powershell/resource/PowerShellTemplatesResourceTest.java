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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Test;

import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.Display;
import com.redhat.rhevm.api.model.DisplayType;
import com.redhat.rhevm.api.model.HighAvailability;
import com.redhat.rhevm.api.model.OperatingSystem;
import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.powershell.enums.PowerShellBootSequence;

import static com.redhat.rhevm.api.powershell.resource.PowerShellTemplatesResource.PROCESS_TEMPLATES;

public class PowerShellTemplatesResourceTest extends AbstractPowerShellCollectionResourceTest<Template, PowerShellTemplateResource, PowerShellTemplatesResource> {

    private static String VM_ID = "vm1";
    private static String VM_NAME = "cookiecutter";
    private static String CLUSTER_ID = "cluster1";
    private static String CLUSTER_NAME = "pleiades";

    private static int MEMSIZE = 1024;
    private static int BOOTSEQUENCE = PowerShellBootSequence.CDN.getValue();
    private static int N_SOCKETS = 2;
    private static int N_CPUS = 4;

    public static final String[] extraArgs = new String[] { CLUSTER_ID, Integer.toString(MEMSIZE), Integer.toString(BOOTSEQUENCE), Integer.toString(N_SOCKETS), Integer.toString(N_CPUS) };

    private static final String ADD_COMMAND_PROLOG = "$v = get-vm \"" + VM_ID + "\";";
    private static final String ADD_COMMAND_NO_CLUSTER_EPILOG = " -mastervm $v";
    private static final String ADD_COMMAND_EPILOG =
        ADD_COMMAND_NO_CLUSTER_EPILOG + " -hostclusterid \"" + CLUSTER_ID + "\"";

    private static final String VM_BY_NAME_ADD_COMMAND_PROLOG = "$v = select-vm -searchtext \"name=" + VM_NAME + "\";";

    private static final String CLUSTER_BY_NAME_ADD_COMMAND_PROLOG =
        "$v = select-vm -searchtext \"name=" + VM_NAME + "\";" +
        "$c = select-cluster -searchtext \"name=" + CLUSTER_NAME + "\";";

    private static final String CLUSTER_BY_NAME_ADD_COMMAND_EPILOG = ADD_COMMAND_NO_CLUSTER_EPILOG + " -hostclusterid $c.ClusterId";

    private static final String HIGHLY_AVAILABLE_ADD_COMMAND_EPILOG = " -highlyavailable ";
    private static final String STATEFUL_ADD_COMMAND_EPILOG = " -stateless ";
    private static final String DISPLAY_TYPE_ADD_COMMAND_EPILOG = " -displaytype VNC";
    private static final String OS_ADD_COMMAND_EPILOG = " -os \"OtherLinux\"";
    private static final String ASYNC_EPILOG =  ASYNC_OPTION  + PROCESS_TEMPLATES + ASYNC_TASKS;

    public PowerShellTemplatesResourceTest() {
        super(new PowerShellTemplateResource("0", null, null, null, null, null), "templates", "template", extraArgs);
    }

    @Test
    public void testList() throws Exception {
        resource.setUriInfo(setUpResourceExpectations(getSelectCommand() + PROCESS_TEMPLATES,
                                                      getSelectReturn(),
                                                      NAMES));
        verifyCollection(resource.list().getTemplates(),
                         NAMES,
                         DESCRIPTIONS);
    }

    @Test
    public void testQuery() throws Exception {
        resource.setUriInfo(setUpResourceExpectations(getQueryCommand(Template.class) + PROCESS_TEMPLATES,
                                                      getQueryReturn(),
                                                      getQueryParam(),
                                                      NAMES_SUBSET));
        verifyCollection(resource.list().getTemplates(),
                         NAMES_SUBSET,
                         DESCRIPTIONS_SUBSET);
    }

    @Test
    public void testAddBlocking() throws Exception {
        setUpHttpHeaderExpectations("Expect", "201-created");

        resource.setUriInfo(setUpAddResourceExpectations(ADD_COMMAND_PROLOG
                                                         + getAddCommand()
                                                         + ADD_COMMAND_EPILOG
                                                         + PROCESS_TEMPLATES,
                                                         getAddReturn(),
                                                         NEW_NAME));
        Response response = resource.add(getModel(NEW_NAME, NEW_DESCRIPTION));
        verifyResponse(response, NEW_NAME, NEW_DESCRIPTION);
        assertEquals(201, response.getStatus());
    }

    @Test
    public void testAddWithVmId() throws Exception {
        setUpHttpHeaderExpectations("Expect", null);

        resource.setUriInfo(setUpAddResourceExpectations(ADD_COMMAND_PROLOG
                                                         + getAddCommand()
                                                         + ADD_COMMAND_EPILOG
                                                         + ASYNC_EPILOG,
                                                         getAddReturn(),
                                                         false,
                                                         NEW_NAME));
        verifyCreated(resource.add(getModel(NEW_NAME, NEW_DESCRIPTION)),
                      Template.class,
                      NEW_NAME,
                      NEW_DESCRIPTION);
    }

    @Test
    public void testAddWithNoCluster() throws Exception {
        setUpHttpHeaderExpectations("Expect", null);

        Template model = getModel(NEW_NAME, NEW_DESCRIPTION);
        model.setCluster(null);
        resource.setUriInfo(setUpAddResourceExpectations(ADD_COMMAND_PROLOG
                                                         + getAddCommand()
                                                         + ADD_COMMAND_NO_CLUSTER_EPILOG
                                                         + ASYNC_EPILOG,
                                                         getAddReturn(),
                                                         false,
                                                         NEW_NAME));

        verifyCreated(resource.add(model), Template.class, NEW_NAME, NEW_DESCRIPTION);
    }

    @Test
    public void testAddWithVmName() throws Exception {
        setUpHttpHeaderExpectations("Expect", null);

        Template model = getModel(NEW_NAME, NEW_DESCRIPTION);
        model.getVm().setId(null);
        model.getVm().setName(VM_NAME);

        resource.setUriInfo(setUpAddResourceExpectations(VM_BY_NAME_ADD_COMMAND_PROLOG
                                                         + getAddCommand()
                                                         + ADD_COMMAND_EPILOG
                                                         + ASYNC_EPILOG,
                                                         getAddReturn(),
                                                         false,
                                                         NEW_NAME));

        verifyCreated(resource.add(model), Template.class, NEW_NAME, NEW_DESCRIPTION);
    }

    @Test
    public void testAddWithClusterName() throws Exception {
        setUpHttpHeaderExpectations("Expect", null);

        Template model = getModel(NEW_NAME, NEW_DESCRIPTION);
        model.getVm().setId(null);
        model.getVm().setName(VM_NAME);
        model.getCluster().setId(null);
        model.getCluster().setName(CLUSTER_NAME);
        resource.setUriInfo(setUpAddResourceExpectations(CLUSTER_BY_NAME_ADD_COMMAND_PROLOG
                                                         + getAddCommand()
                                                         + CLUSTER_BY_NAME_ADD_COMMAND_EPILOG
                                                         + ASYNC_EPILOG,
                                                         getAddReturn(),
                                                         false,
                                                         NEW_NAME));

        verifyCreated(resource.add(model), Template.class, NEW_NAME, NEW_DESCRIPTION);
    }

    @Test
    public void testAddHighlyAvailable() throws Exception {
        setUpHttpHeaderExpectations("Expect", null);

        resource.setUriInfo(setUpAddResourceExpectations(ADD_COMMAND_PROLOG
                                                         + getAddCommand()
                                                         + ADD_COMMAND_EPILOG
                                                         + HIGHLY_AVAILABLE_ADD_COMMAND_EPILOG
                                                         + ASYNC_EPILOG,
                                                         getAddReturn(),
                                                         false,
                                                         NEW_NAME));
        Template template = getModel(NEW_NAME, NEW_DESCRIPTION);
        template.setHighAvailability(new HighAvailability());
        template.getHighAvailability().setEnabled(true);
        verifyCreated(resource.add(template),
                      Template.class,
                      NEW_NAME,
                      NEW_DESCRIPTION);
    }

    @Test
    public void testAddStateless() throws Exception {
        setUpHttpHeaderExpectations("Expect", null);

        resource.setUriInfo(setUpAddResourceExpectations(ADD_COMMAND_PROLOG
                                                         + getAddCommand()
                                                         + ADD_COMMAND_EPILOG
                                                         + STATEFUL_ADD_COMMAND_EPILOG
                                                         + ASYNC_EPILOG,
                                                         getAddReturn(),
                                                         false,
                                                         NEW_NAME));
        Template template = getModel(NEW_NAME, NEW_DESCRIPTION);
        template.setStateless(false);
        verifyCreated(resource.add(template),
                      Template.class,
                      NEW_NAME,
                      NEW_DESCRIPTION);
    }

    @Test
    public void testAddWithDisplayType() throws Exception {
        setUpHttpHeaderExpectations("Expect", null);

        resource.setUriInfo(setUpAddResourceExpectations(ADD_COMMAND_PROLOG
                                                         + getAddCommand()
                                                         + ADD_COMMAND_EPILOG
                                                         + DISPLAY_TYPE_ADD_COMMAND_EPILOG
                                                         + ASYNC_EPILOG,
                                                         getAddReturn(),
                                                         false,
                                                         NEW_NAME));
        Template template = getModel(NEW_NAME, NEW_DESCRIPTION);
        template.setDisplay(new Display());
        template.getDisplay().setType(DisplayType.VNC.value());
        verifyCreated(resource.add(template),
                      Template.class,
                      NEW_NAME,
                      NEW_DESCRIPTION);
    }

    @Test
    public void testAddWithOperatingSystem() throws Exception {
        setUpHttpHeaderExpectations("Expect", null);

        resource.setUriInfo(setUpAddResourceExpectations(ADD_COMMAND_PROLOG
                                                         + getAddCommand()
                                                         + ADD_COMMAND_EPILOG
                                                         + OS_ADD_COMMAND_EPILOG
                                                         + ASYNC_EPILOG,
                                                         getAddReturn(),
                                                         false,
                                                         NEW_NAME));
        Template template = getModel(NEW_NAME, NEW_DESCRIPTION);
        template.setOs(new OperatingSystem());
        template.getOs().setType("OtherLinux");
        verifyCreated(resource.add(template),
                      Template.class,
                      NEW_NAME,
                      NEW_DESCRIPTION);
    }

    @Test
    public void testAddIncompleteParameters() throws Exception {
        Template model = new Template();
        model.setName(NEW_NAME);
        resource.setUriInfo(setUpResourceExpectations(new String[]{}, new String[]{}, false, null));
        try {
            resource.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "Template", "add", "vm.id|name");
        }
    }

    @Test
    public void testRemove() throws Exception {
        resource.setUriInfo(setUpResourceExpectations(getRemoveCommand(), null));
        resource.remove(Integer.toString(NAMES[1].hashCode()));
    }

    @Test
    public void testGetSubResource() throws Exception {
        resource.setUriInfo(setUpResourceExpectations(null, null));
        verifyResource(
            (PowerShellTemplateResource)resource.getTemplateSubResource(Integer.toString(NEW_NAME.hashCode())),
            NEW_NAME);
    }

    protected PowerShellTemplatesResource getResource() {
        return new PowerShellTemplatesResource();
    }

    protected void populateModel(Template template) {
        VM vm = new VM();
        vm.setId(VM_ID);
        template.setVm(vm);

        Cluster cluster = new Cluster();
        cluster.setId(CLUSTER_ID);
        template.setCluster(cluster);
    }

}

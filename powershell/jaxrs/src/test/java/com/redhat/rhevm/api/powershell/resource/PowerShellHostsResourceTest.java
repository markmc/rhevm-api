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

import java.util.List;

import javax.ws.rs.WebApplicationException;

import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.model.PowerManagement;
import com.redhat.rhevm.api.model.PowerManagementOption;
import com.redhat.rhevm.api.model.PowerManagementOptions;

import org.junit.Test;

import static com.redhat.rhevm.api.powershell.resource.PowerShellHostsResource.PROCESS_HOSTS;
import static com.redhat.rhevm.api.powershell.resource.PowerShellHostsResource.PROCESS_HOSTS_STATS;

public class PowerShellHostsResourceTest extends AbstractPowerShellCollectionResourceTest<Host, PowerShellHostResource, PowerShellHostsResource> {

    private static final String CLUSTER_ID = "1234-5678-8765-4321";
    private static final String CLUSTER_NAME = "pleiades";
    private static final String ADDRESS = "192.168.105.1";
    private static final String PASSWORD = "foobar";
    private static final int PORT = 12345;

    public static final String[] extraArgs = new String[] { CLUSTER_ID, ADDRESS, Integer.toString(PORT) };

    private static final String NO_DESCRIPTION = null;
    private static final String[] NO_DESCRIPTIONS = new String[] { null, null, null };
    private static final String[] NO_DESCRIPTIONS_SUBSET = new String[] { null, null };

    private static final String STATUS_QUERY = "status=inactive";
    private static final String TRANSFORMED_STATUS_QUERY = "status=maintenance";

    private static final String CLUSTER_BY_NAME_ADD_COMMAND_PROLOG =
        "$c = select-cluster -searchtext \"name=" + CLUSTER_NAME + "\";";

    private static final String ADD_COMMAND = "$h = add-host -name \"" + NEW_NAME + "\" ";
    private static final String ADD_COMMAND_EPILOG = "-address \"" + ADDRESS + "\" -rootpassword \"" + PASSWORD + "\"";
    private static final String ADD_COMMAND_PM_EPILOG = " -managementtype \"fenceme\" -managementhostname \"foo\" -managementuser \"me\" -managementpassword \"mysecret\" -managementsecure -managementport \"12345\" -managementslot \"54321\" -managementoptions \"secure=true,port=12345,slot=54321\"";
    private static final String CLUSTER_BY_NAME_ADD_COMMAND_EPILOG = ADD_COMMAND_EPILOG + " -hostclusterid $c.ClusterId";
    private static final String CLUSTER_BY_ID_ADD_COMMAND_EPILOG = ADD_COMMAND_EPILOG + " -hostclusterid \"" + CLUSTER_ID + "\"";
    private static final String PORT_OVERRIDE_ADD_COMMAND_EPILOG = ADD_COMMAND_EPILOG + " -port " + PORT;


    public PowerShellHostsResourceTest() {
        super(new PowerShellHostResource("0", null, null, null, null, null), "hosts", "host", extraArgs);
    }


    @Test
    public void testList() throws Exception {
        setUpHttpHeaderNullExpectations("Accept");
        resource.setUriInfo(setUpResourceExpectations(getSelectCommand() + PROCESS_HOSTS,
                                                      getSelectReturn(),
                                                      NAMES));
        verifyCollection(resource.list().getHosts(), NAMES, NO_DESCRIPTIONS);
    }

    @Test
    public void testListIncludeStats() throws Exception {
        setUpHttpHeaderExpectations("Accept", "application/xml; detail=statistics");
        resource.setUriInfo(setUpResourceExpectations(getSelectCommand() + PROCESS_HOSTS_STATS,
                                                      getSelectReturn(),
                                                      NAMES));
        List<Host> hosts = resource.list().getHosts();
        assertTrue(hosts.get(0).isSetStatistics());
        verifyCollection(hosts, NAMES, NO_DESCRIPTIONS);
    }

    @Test
    public void testQuery() throws Exception {
        setUpHttpHeaderNullExpectations("Accept");
        resource.setUriInfo(setUpResourceExpectations(getQueryCommand(Host.class) + PROCESS_HOSTS,
                                                      getQueryReturn(),
                                                      getQueryParam(),
                                                      NAMES_SUBSET));
        verifyCollection(resource.list().getHosts(), NAMES_SUBSET, NO_DESCRIPTIONS_SUBSET);
    }

    @Test
    public void testStatusQuery() throws Exception {
        setUpHttpHeaderNullExpectations("Accept");
        resource.setUriInfo(setUpResourceExpectations(getQueryCommand(Host.class, TRANSFORMED_STATUS_QUERY) + PROCESS_HOSTS,
                                                      getQueryReturn(),
                                                      getQueryParam(STATUS_QUERY),
                                                      NAMES_SUBSET));
        verifyCollection(resource.list().getHosts(), NAMES_SUBSET, NO_DESCRIPTIONS_SUBSET);
    }

    @Test
    public void testAdd() throws Exception {
        setUpHttpHeaderNullExpectations("Accept");
        resource.setUriInfo(setUpAddResourceExpectations(ADD_COMMAND + ADD_COMMAND_EPILOG + ";$h" + PROCESS_HOSTS,
                                                         getAddReturn(),
                                                         NEW_NAME));
        verifyResponse(resource.add(getModel(NEW_NAME, NO_DESCRIPTION)), NEW_NAME, NO_DESCRIPTION);
    }

    @Test
    public void testAddWithClusterName() throws Exception {
        setUpHttpHeaderNullExpectations("Accept");
        Host model = getModel(NEW_NAME, NO_DESCRIPTION);
        model.setCluster(new Cluster());
        model.getCluster().setName(CLUSTER_NAME);
        resource.setUriInfo(setUpAddResourceExpectations(CLUSTER_BY_NAME_ADD_COMMAND_PROLOG
                                                         + ADD_COMMAND
                                                         + CLUSTER_BY_NAME_ADD_COMMAND_EPILOG
                                                         + ";$h" + PROCESS_HOSTS,
                                                         getAddReturn(),
                                                         NEW_NAME));

        verifyResponse(resource.add(model), NEW_NAME, NO_DESCRIPTION);
    }

    @Test
    public void testAddWithClusterId() throws Exception {
        setUpHttpHeaderNullExpectations("Accept");
        Host model = getModel(NEW_NAME, NO_DESCRIPTION);
        model.setCluster(new Cluster());
        model.getCluster().setId(CLUSTER_ID);
        resource.setUriInfo(setUpAddResourceExpectations(ADD_COMMAND
                                                         + CLUSTER_BY_ID_ADD_COMMAND_EPILOG
                                                         + ";$h" + PROCESS_HOSTS,
                                                         getAddReturn(),
                                                         NEW_NAME));

        verifyResponse(resource.add(model), NEW_NAME, NO_DESCRIPTION);
    }

    @Test
    public void testAddWithPowerManagement() throws Exception {
        setUpHttpHeaderNullExpectations("Accept");
        Host model = getModel(NEW_NAME, NO_DESCRIPTION);

        model.setPowerManagement(new PowerManagement());
        model.getPowerManagement().setEnabled(false);
        model.getPowerManagement().setType("fenceme");
        model.getPowerManagement().setAddress("foo");
        model.getPowerManagement().setUsername("me");
        model.getPowerManagement().setPassword("mysecret");
        model.getPowerManagement().setOptions(new PowerManagementOptions());
        model.getPowerManagement().getOptions().getOptions().add(buildOption("secure", "true"));
        model.getPowerManagement().getOptions().getOptions().add(buildOption("port", "12345"));
        model.getPowerManagement().getOptions().getOptions().add(buildOption("slot", "54321"));

        resource.setUriInfo(setUpAddResourceExpectations(ADD_COMMAND + ADD_COMMAND_EPILOG + ADD_COMMAND_PM_EPILOG + ";$h" + PROCESS_HOSTS,
                                                         getAddReturn(),
                                                         NEW_NAME));

        verifyResponse(resource.add(model), NEW_NAME, NO_DESCRIPTION);
    }

    private PowerManagementOption buildOption(String name, String value) {
        PowerManagementOption option = new PowerManagementOption();
        option.setName(name);
        option.setValue(value);
        return option;
    }

    @Test
    public void testAddIncompleteParameters() throws Exception {
        Host model = new Host();
        model.setName(NEW_NAME);
        resource.setUriInfo(setUpResourceExpectations(new String[]{}, new String[]{}, false, null));
        try {
            resource.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "Host", "add", "address", "rootPassword");
        }
    }

    @Test
    public void testAddWithPort() throws Exception {
        setUpHttpHeaderNullExpectations("Accept");
        Host model = getModel(NEW_NAME, NO_DESCRIPTION);
        model.setPort(PORT);
        resource.setUriInfo(setUpAddResourceExpectations(ADD_COMMAND
                                                         + PORT_OVERRIDE_ADD_COMMAND_EPILOG
                                                         + ";$h" + PROCESS_HOSTS,
                                                         getAddReturn(),
                                                         NEW_NAME));

        verifyResponse(resource.add(model), NEW_NAME, NO_DESCRIPTION);
    }

    @Test
    public void testRemove() throws Exception {
        setUpResourceExpectations(getRemoveCommand(), null);
        resource.remove(Integer.toString(NAMES[1].hashCode()));
    }

    @Test
    public void testGetSubResource() throws Exception {
        resource.setUriInfo(setUpResourceExpectations(null, null));
        verifyResource(
            (PowerShellHostResource)resource.getHostSubResource(Integer.toString(NEW_NAME.hashCode())),
            NEW_NAME);
    }

    protected PowerShellHostsResource getResource() {
        return new PowerShellHostsResource();
    }

    protected void populateModel(Host host) {
        host.setAddress(ADDRESS);
        host.setRootPassword(PASSWORD);
    }
}

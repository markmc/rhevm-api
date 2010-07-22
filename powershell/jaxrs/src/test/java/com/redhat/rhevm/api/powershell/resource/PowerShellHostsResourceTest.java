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

import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.model.Hosts;

import org.junit.Test;

public class PowerShellHostsResourceTest extends AbstractPowerShellCollectionResourceTest<Host, PowerShellHostResource, PowerShellHostsResource> {

    private static final String CLUSTER_ID = "1234-5678-8765-4321";
    private static final String CLUSTER_NAME = "pleiades";
    private static final String ADDRESS = "192.168.105.1";
    private static final int PORT = 12345;

    public static final String[] extraArgs = new String[] { CLUSTER_ID, ADDRESS, Integer.toString(PORT) };

    private static final String NO_DESCRIPTION = null;
    private static final String[] NO_DESCRIPTIONS = new String[] { null, null, null };
    private static final String[] NO_DESCRIPTIONS_SUBSET = new String[] { null, null };

    private static final String CLUSTER_BY_NAME_ADD_COMMAND_PROLOG =
        "$c = select-cluster -searchtext \"name=" + CLUSTER_NAME + "\";";

    private static final String ADD_COMMAND = "add-host -name \"" + NEW_NAME + "\" ";
    private static final String ADD_COMMAND_EPILOG = "-address \"" + ADDRESS + "\" -rootpassword notneeded";
    private static final String CLUSTER_BY_NAME_ADD_COMMAND_EPILOG = ADD_COMMAND_EPILOG + " -hostclusterid $c.ClusterId";
    private static final String CLUSTER_BY_ID_ADD_COMMAND_EPILOG = ADD_COMMAND_EPILOG + " -hostclusterid \"" + CLUSTER_ID + "\"";
    private static final String PORT_OVERRIDE_ADD_COMMAND_EPILOG = ADD_COMMAND_EPILOG + " -port " + PORT;


    public PowerShellHostsResourceTest() {
        super(new PowerShellHostResource("0", null, null, null), "hosts", "host", extraArgs);
    }

    @Test
    public void testList() throws Exception {
        verifyCollection(
            resource.list(setUpResourceExpectations(getSelectCommand(),
                                                    getSelectReturn(),
                                                    NAMES)).getHosts(),
            NAMES, NO_DESCRIPTIONS);
    }

    @Test
    public void testQuery() throws Exception {
        verifyCollection(
            resource.list(setUpResourceExpectations(getQueryCommand(Host.class),
                                                    getQueryReturn(),
                                                    getQueryParam(),
                                                    NAMES_SUBSET)).getHosts(),
            NAMES_SUBSET, NO_DESCRIPTIONS_SUBSET);
    }

    @Test
    public void testAdd() throws Exception {
        verifyResponse(
            resource.add(setUpAddResourceExpectations(ADD_COMMAND + ADD_COMMAND_EPILOG,
                                                      getAddReturn(),
                                                      NEW_NAME),
                         getModel(NEW_NAME, NO_DESCRIPTION)),
            NEW_NAME, NO_DESCRIPTION);
    }

    @Test
    public void testAddWithClusterName() throws Exception {
        Host model = getModel(NEW_NAME, NO_DESCRIPTION);
        model.setCluster(new Cluster());
        model.getCluster().setName(CLUSTER_NAME);

        verifyResponse(
            resource.add(setUpAddResourceExpectations(CLUSTER_BY_NAME_ADD_COMMAND_PROLOG
                                                      + ADD_COMMAND
                                                      + CLUSTER_BY_NAME_ADD_COMMAND_EPILOG,
                                                      getAddReturn(),
                                                      NEW_NAME),
                         model),
            NEW_NAME, NO_DESCRIPTION);
    }

    @Test
    public void testAddWithClusterId() throws Exception {
        Host model = getModel(NEW_NAME, NO_DESCRIPTION);
        model.setCluster(new Cluster());
        model.getCluster().setId(CLUSTER_ID);

        verifyResponse(
            resource.add(setUpAddResourceExpectations(ADD_COMMAND
                                                      + CLUSTER_BY_ID_ADD_COMMAND_EPILOG,
                                                      getAddReturn(),
                                                      NEW_NAME),
                         model),
            NEW_NAME, NO_DESCRIPTION);
    }

    @Test
    public void testAddWithPort() throws Exception {
        Host model = getModel(NEW_NAME, NO_DESCRIPTION);
        model.setPort(PORT);

        verifyResponse(
            resource.add(setUpAddResourceExpectations(ADD_COMMAND
                                                      + PORT_OVERRIDE_ADD_COMMAND_EPILOG,
                                                      getAddReturn(),
                                                      NEW_NAME),
                         model),
            NEW_NAME, NO_DESCRIPTION);
    }

    @Test
    public void testRemove() throws Exception {
        setUpResourceExpectations(getRemoveCommand(), null);
        resource.remove(Integer.toString(NAMES[1].hashCode()));
    }

    @Test
    public void testGetSubResource() throws Exception {
        verifyResource(
            (PowerShellHostResource)resource.getHostSubResource(setUpResourceExpectations(null, null),
                                                                Integer.toString(NEW_NAME.hashCode())),
            NEW_NAME);
    }

    protected PowerShellHostsResource getResource() {
        return new PowerShellHostsResource();
    }

    protected void populateModel(Host host) {
        host.setAddress(ADDRESS);
    }
}

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
import java.text.MessageFormat;
import java.util.concurrent.Executor;

import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.UriBuilder;

import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.Network;
import com.redhat.rhevm.api.model.Networks;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellException;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPool;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;

import org.junit.Test;

import org.junit.runner.RunWith;

import static org.easymock.classextension.EasyMock.expect;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { PowerShellCmd.class })
public class PowerShellClusterNetworksResourceTest
    extends AbstractPowerShellResourceTest<Cluster, PowerShellClusterResource> {

    private static final String CLUSTER_NAME = "GeneralCuster";
    private static final String[] NETWORKS = { "abc", "cbs", "nbc" };

    private static final String DATA_CENTER_ID = "54321";

    private static final String[][] extraArgs = new String[][] {
        { DATA_CENTER_ID },
        { DATA_CENTER_ID },
        { DATA_CENTER_ID },
    };

    private static final String GET_NETWORKS_CMD = "$cluster = select-cluster | ? { $_.clusterid -eq \"" + asId(CLUSTER_NAME) + "\" }; $cluster.getnetworks()";

    private static final String GET_NETWORK_CMD = GET_NETWORKS_CMD + " | ? { $_.networkid -eq \"" + asId(NETWORKS[0]) + "\" }";

    private static final String LOOKUP_CLUSTER = "$cluster = select-cluster | ? { $_.clusterid -eq \"" + asId(CLUSTER_NAME) + "\" }; ";
    private static final String LOOKUP_NETWORK = "$net = get-networks | ? { $_.networkid -eq \"" + asId(NETWORKS[0]) + "\" }; ";
    private static final String LOOKUP_NETWORK_BY_NAME = "$net = get-networks | ? { $_.name -eq \"" + NETWORKS[0] + "\" }; ";
    private static final String ADD_REMOVE_PROLOG = LOOKUP_CLUSTER + LOOKUP_NETWORK;
    private static final String ADD_REMOVE_EPILOG = " -clusterobject $cluster -networkobject $net";
    private static final String ADD_EPILOG = ADD_REMOVE_EPILOG + "; $cluster.getnetworks() | ? { $_.networkid -eq $net.networkid }";

    private static final String ADD_NETWORK_CMD = ADD_REMOVE_PROLOG + "$cluster = add-networktocluster" + ADD_EPILOG;
    private static final String REMOVE_NETWORK_CMD = ADD_REMOVE_PROLOG + "$cluster = remove-networkfromcluster" + ADD_REMOVE_EPILOG;

    private static final String ADD_NETWORK_BY_NAME_CMD = LOOKUP_CLUSTER + LOOKUP_NETWORK_BY_NAME + "$cluster = add-networktocluster" + ADD_EPILOG;

    protected  PowerShellClusterResource getResource(Executor executor, PowerShellPoolMap poolMap, PowerShellParser parser) {
        return new PowerShellClusterResource(asId(CLUSTER_NAME), executor, poolMap, parser);
    }

    protected String formatNetworks(String[] names, String[][] args) {
        String[] descriptions = new String[names.length];
        return formatXmlReturn("network", names, descriptions, args);
    }

    protected String formatNetwork(String name, String[] args) {
        return formatNetworks(asArray(name), asArray(args));
    }

    @Test
    public void testGet() {
        PowerShellClusterNetworksResource parent = new PowerShellClusterNetworksResource(asId(CLUSTER_NAME), executor, poolMap, parser);
        PowerShellClusterNetworkResource resource = new PowerShellClusterNetworkResource(asId(NETWORKS[0]), parent);

        setUpCmdExpectations(GET_NETWORK_CMD,
                             formatNetwork(NETWORKS[0], extraArgs[0]));
        replayAll();

        verifyNetwork(resource.get(), 0);
    }

    @Test
    public void testList() {
        PowerShellClusterNetworksResource resource = new PowerShellClusterNetworksResource(asId(CLUSTER_NAME), executor, poolMap, parser);

        setUpCmdExpectations(GET_NETWORKS_CMD, formatNetworks(NETWORKS, extraArgs));
        replayAll();

        verifyNetworks(resource.list());
    }

    @Test
    public void testAdd() throws Exception {
        PowerShellClusterNetworksResource resource = new PowerShellClusterNetworksResource(asId(CLUSTER_NAME), executor, poolMap, parser);

        setUpCmdExpectations(ADD_NETWORK_CMD, formatNetwork(NETWORKS[0], extraArgs[0]));
        UriInfo uriInfo = setUpUriInfoExpections(asId(NETWORKS[0]));
        replayAll();

        Network network = new Network();
        network.setId(asId(NETWORKS[0]));

        verifyNetwork((Network)resource.add(uriInfo, network).getEntity(), 0);
    }

    @Test
    public void testAddByName() throws Exception {
        PowerShellClusterNetworksResource resource = new PowerShellClusterNetworksResource(asId(CLUSTER_NAME), executor, poolMap, parser);

        setUpCmdExpectations(ADD_NETWORK_BY_NAME_CMD, formatNetwork(NETWORKS[0], extraArgs[0]));
        UriInfo uriInfo = setUpUriInfoExpections(asId(NETWORKS[0]));
        replayAll();

        Network network = new Network();
        network.setName(NETWORKS[0]);

        verifyNetwork((Network)resource.add(uriInfo, network).getEntity(), 0);
    }

    @Test
    public void testRemove() {
        PowerShellClusterNetworksResource resource = new PowerShellClusterNetworksResource(asId(CLUSTER_NAME), executor, poolMap, parser);

        setUpCmdExpectations(REMOVE_NETWORK_CMD, "");
        replayAll();

        resource.remove(asId(NETWORKS[0]));
    }

    private UriInfo setUpUriInfoExpections(String id) throws Exception {
        UriInfo uriInfo = createMock(UriInfo.class);
        UriBuilder uriBuilder = createMock(UriBuilder.class);
        expect(uriInfo.getAbsolutePathBuilder()).andReturn(uriBuilder);
        expect(uriBuilder.path(id)).andReturn(uriBuilder);
        expect(uriBuilder.build()).andReturn(new URI("clusters/" + asId(CLUSTER_NAME) + "/networks/" + id)).anyTimes();
        return uriInfo;
    }

    private void setUpCmdExpectations(String command, String ret) {
        setUpCmdExpectations(asArray(command), asArray(ret));
    }

    private void setUpCmdExpectations(String commands[], String returns[]) {
        mockStatic(PowerShellCmd.class);
        for (int i = 0 ; i < Math.min(commands.length, returns.length) ; i++) {
            if (commands[i] != null) {
                expect(PowerShellCmd.runCommand(setUpPoolExpectations(), commands[i])).andReturn(returns[i]);
            }
        }
    }

    protected PowerShellPool setUpPoolExpectations() {
        PowerShellPool pool = createMock(PowerShellPool.class);
        expect(poolMap.get()).andReturn(pool);
        return pool;
    }

    private void verifyNetwork(Network network, int i) {
        assertNotNull(network);
        assertEquals(asId(NETWORKS[i]), network.getId());
    }

    private void verifyNetworks(Networks networks) {
        assertNotNull(networks);
        assertEquals(NETWORKS.length, networks.getNetworks().size());
        for (int i = 0; i < NETWORKS.length; i++) {
            verifyNetwork(networks.getNetworks().get(i), i);
        }
    }
}

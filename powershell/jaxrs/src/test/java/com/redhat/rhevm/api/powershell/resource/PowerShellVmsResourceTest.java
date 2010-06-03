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

import java.text.MessageFormat;

import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.model.VMs;

import org.junit.Test;

public class PowerShellVmsResourceTest extends AbstractPowerShellCollectionResourceTest<VM, PowerShellVmResource, PowerShellVmsResource> {

    private static String TEMPLATE_ID = "template1";
    private static String CLUSTER_ID = "cluster1";

    private static final String ADD_COMMAND_PROLOG =
        "$templ = get-template -templateid " + TEMPLATE_ID + "\n";
    private static final String ADD_COMMAND_EPILOG =
        "-templateobject $templ -hostclusterid " + CLUSTER_ID;

    private static final String SELECT_RETURN_EPILOG = "\nhostclusterid: " + CLUSTER_ID + "\ntemplateid: " + TEMPLATE_ID;
    private static final String ADD_RETURN_EPILOG    = "\nhostclusterid: " + CLUSTER_ID + "\ntemplateid: " + TEMPLATE_ID;

    private static final String GET_DISKS_COMMAND = "$v = get-vm {0,number,#}\n$v.GetDiskImages()\n";
    public static final String GET_DISKS_RETURN = "snapshotid: 0\nactualsizeinbytes: 10485760\ndisktype: system\nstatus: ok\ndiskinterface: ide\nvolumeformat: raw\nvolumetype: sparse\nboot: true\nwipeafterdelete: false\npropagateerrors: off\n";

    private static final String GET_INTERFACES_COMMAND = "$v = get-vm {0,number,#}\n$v.GetNetworkAdapters()\n";
    public static final String GET_INTERFACES_RETURN = "id: 1\nname: eth1\nnetwork: net1\ntype: pv\nmacaddress: 00:1a:4a:16:84:02\naddress: 172.31.0.10\nsubnet: 255.255.255.0\ngateway: 172.31.0.1\n";

    public static final String LOOKUP_NETWORK_ID_COMMAND = "$n = get-networks\nforeach ($i in $n) {  if ($i.name -eq 'net1') {    $i  }}";
    public static final String LOOKUP_NETWORK_ID_RETURN = "networkid: 666\nname: net1\ndatacenterid: 999";

    public PowerShellVmsResourceTest() {
        super(new PowerShellVmResource("0", null), "vms", "vm");
    }

    @Test
    public void testList() throws Exception {
        String [] commands = { getSelectCommand(),
                               MessageFormat.format(GET_DISKS_COMMAND, NAMES[0].hashCode()),
                               MessageFormat.format(GET_INTERFACES_COMMAND, NAMES[0].hashCode()),
                               LOOKUP_NETWORK_ID_COMMAND,
                               MessageFormat.format(GET_DISKS_COMMAND, NAMES[1].hashCode()),
                               MessageFormat.format(GET_INTERFACES_COMMAND, NAMES[1].hashCode()),
                               LOOKUP_NETWORK_ID_COMMAND,
                               MessageFormat.format(GET_DISKS_COMMAND, NAMES[2].hashCode()),
                               MessageFormat.format(GET_INTERFACES_COMMAND, NAMES[2].hashCode()),
                               LOOKUP_NETWORK_ID_COMMAND };
        String [] returns = { getSelectReturn(SELECT_RETURN_EPILOG),
                              GET_DISKS_RETURN,
                              GET_INTERFACES_RETURN,
                              LOOKUP_NETWORK_ID_RETURN,
                              GET_DISKS_RETURN,
                              GET_INTERFACES_RETURN,
                              LOOKUP_NETWORK_ID_RETURN,
                              GET_DISKS_RETURN,
                              GET_INTERFACES_RETURN,
                              LOOKUP_NETWORK_ID_RETURN };

        verifyCollection(
            resource.list(setUpResourceExpectations(commands, returns, 3, NAMES)).getVMs(),
            NAMES);
    }

    @Test
    public void testQuery() throws Exception {
        String [] commands = { getQueryCommand(VMs.class),
                               MessageFormat.format(GET_DISKS_COMMAND, NAMES[1].hashCode()),
                               MessageFormat.format(GET_INTERFACES_COMMAND, NAMES[1].hashCode()),
                               LOOKUP_NETWORK_ID_COMMAND,
                               MessageFormat.format(GET_DISKS_COMMAND, NAMES[2].hashCode()),
                               MessageFormat.format(GET_INTERFACES_COMMAND, NAMES[2].hashCode()),
                               LOOKUP_NETWORK_ID_COMMAND };

        String [] returns = { getQueryReturn(SELECT_RETURN_EPILOG),
                              GET_DISKS_RETURN,
                              GET_INTERFACES_RETURN,
                              LOOKUP_NETWORK_ID_RETURN,
                              GET_DISKS_RETURN,
                              GET_INTERFACES_RETURN,
                              LOOKUP_NETWORK_ID_RETURN };

        verifyCollection(
            resource.list(setUpResourceExpectations(commands, returns, 2, getQueryParam(), NAMES_SUBSET)).getVMs(),
            NAMES_SUBSET);
    }

    @Test
    public void testAdd() throws Exception {
        String [] commands = { ADD_COMMAND_PROLOG + getAddCommand() + ADD_COMMAND_EPILOG,
                               MessageFormat.format(GET_DISKS_COMMAND, NEW_NAME.hashCode()),
                               MessageFormat.format(GET_INTERFACES_COMMAND, NEW_NAME.hashCode()),
                               LOOKUP_NETWORK_ID_COMMAND };

        String [] returns = { getAddReturn(ADD_RETURN_EPILOG),
                              GET_DISKS_RETURN,
                              GET_INTERFACES_RETURN,
                              LOOKUP_NETWORK_ID_RETURN };

        verifyResponse(
            resource.add(setUpResourceExpectations(commands, returns, 1, NEW_NAME),
                         getModel(NEW_NAME)),
            NEW_NAME);
    }

    @Test
    public void testRemove() throws Exception {
        setUpResourceExpectations(getRemoveCommand(), null);
        resource.remove(Integer.toString(NAMES[1].hashCode()));
    }

    @Test
    public void testGetSubResource() throws Exception {
        verifyResource(
            (PowerShellVmResource)resource.getVmSubResource(setUpResourceExpectations(null, null),
                                                            Integer.toString(NEW_NAME.hashCode())),
            NEW_NAME);
    }

    protected PowerShellVmsResource getResource() {
        return new PowerShellVmsResource();
    }

    protected void populateModel(VM vm) {
        Template template = new Template();
        template.setId(TEMPLATE_ID);
        vm.setTemplate(template);

        Cluster cluster = new Cluster();
        cluster.setId(CLUSTER_ID);
        vm.setCluster(cluster);
    }
}

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

import org.junit.Test;

import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.model.VM;


public class PowerShellTemplatesResourceTest extends AbstractPowerShellCollectionResourceTest<Template, PowerShellTemplateResource, PowerShellTemplatesResource> {

    private static String VM_ID = "vm1";
    private static String VM_NAME = "cookiecutter";
    private static String CLUSTER_ID = "cluster1";
    private static String CLUSTER_NAME = "pleiades";

    private static final String ADD_COMMAND_PROLOG = "$v = get-vm \"" + VM_ID + "\"\n";
    private static final String ADD_COMMAND_NO_CLUSTER_EPILOG = "-mastervm $v";
    private static final String ADD_COMMAND_EPILOG =
        ADD_COMMAND_NO_CLUSTER_EPILOG + " -hostclusterid \"" + CLUSTER_ID + "\"";

    private static final String VM_BY_NAME_ADD_COMMAND_PROLOG = "$v = select-vm -searchtext \"name=" + VM_NAME + "\"\n";

    private static final String CLUSTER_BY_NAME_ADD_COMMAND_PROLOG =
        "$v = select-vm -searchtext \"name=" + VM_NAME + "\"\n" +
        "$c = select-cluster -searchtext \"name=" + CLUSTER_NAME + "\"\n";

    private static final String CLUSTER_BY_NAME_ADD_COMMAND_EPILOG = ADD_COMMAND_NO_CLUSTER_EPILOG + " -hostclusterid $c.ClusterId";

    private static final String OTHER_PROPS = "memsizemb: 1024\ndefaultbootsequence: CDN\nnumofsockets: 2\nnumofcpuspersocket: 4\n";

    private static final String SELECT_RETURN_EPILOG = "\n" + OTHER_PROPS;
    private static final String ADD_NO_CLUSTER_RETURN_EPILOG = "\nvmid: " + VM_ID + "\n" + OTHER_PROPS;
    private static final String ADD_RETURN_EPILOG = "\nhostclusterid: " + CLUSTER_ID + ADD_NO_CLUSTER_RETURN_EPILOG;

    public PowerShellTemplatesResourceTest() {
        super(new PowerShellTemplateResource("0", null, null), "templates", "template");
    }

    @Test
    public void testList() throws Exception {
        verifyCollection(
            resource.list(setUpResourceExpectations(getSelectCommand(),
                                                    getSelectReturn(SELECT_RETURN_EPILOG),
                                                    NAMES)).getTemplates(),
            NAMES);
    }

    @Test
    public void testQuery() throws Exception {
        verifyCollection(
            resource.list(setUpResourceExpectations(getQueryCommand(Template.class),
                                                    getQueryReturn(SELECT_RETURN_EPILOG),
                                                    getQueryParam(),
                                                    NAMES_SUBSET)).getTemplates(),
            NAMES_SUBSET);
    }

    @Test
    public void testAddWithVmId() throws Exception {
        verifyResponse(
            resource.add(setUpAddResourceExpectations(ADD_COMMAND_PROLOG + getAddCommand(true) + ADD_COMMAND_EPILOG,
                                                      getAddReturn(ADD_RETURN_EPILOG),
                                                      NEW_NAME),
                         getModel(NEW_NAME)),
            NEW_NAME);
    }

    @Test
    public void testAddWithNoCluster() throws Exception {
        Template model = getModel(NEW_NAME);
        model.setCluster(null);

        verifyResponse(
            resource.add(setUpAddResourceExpectations(ADD_COMMAND_PROLOG + getAddCommand(true) + ADD_COMMAND_NO_CLUSTER_EPILOG,
                                                      getAddReturn(ADD_NO_CLUSTER_RETURN_EPILOG),
                                                      NEW_NAME),
                         model),
            NEW_NAME);
    }

    @Test
    public void testAddWithVmName() throws Exception {
        Template model = getModel(NEW_NAME);
        model.getVm().setId(null);
        model.getVm().setName(VM_NAME);

        verifyResponse(
            resource.add(setUpAddResourceExpectations(VM_BY_NAME_ADD_COMMAND_PROLOG + getAddCommand(true) + ADD_COMMAND_EPILOG,
                                                      getAddReturn(ADD_RETURN_EPILOG),
                                                      NEW_NAME),
                         model),
            NEW_NAME);
    }

    @Test
    public void testAddWithClusterName() throws Exception {
        Template model = getModel(NEW_NAME);
        model.getVm().setId(null);
        model.getVm().setName(VM_NAME);
        model.getCluster().setId(null);
        model.getCluster().setName(CLUSTER_NAME);

        verifyResponse(
            resource.add(setUpAddResourceExpectations(CLUSTER_BY_NAME_ADD_COMMAND_PROLOG + getAddCommand(true) + CLUSTER_BY_NAME_ADD_COMMAND_EPILOG,
                                                      getAddReturn(ADD_RETURN_EPILOG),
                                                      NEW_NAME),
                         model),
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
            (PowerShellTemplateResource)resource.getTemplateSubResource(setUpResourceExpectations(null, null),
                                                                        Integer.toString(NEW_NAME.hashCode())),
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

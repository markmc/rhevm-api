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

import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.model.VmPool;

import org.junit.Test;


public class PowerShellVmPoolsResourceTest extends AbstractPowerShellCollectionResourceTest<VmPool, PowerShellVmPoolResource, PowerShellVmPoolsResource> {

    public static final String CLUSTER_NAME = "Default";
    public static final String CLUSTER_ID = Integer.toString(CLUSTER_NAME.hashCode());
    public static final String TEMPLATE_NAME = "foo";
    public static final String TEMPLATE_ID = Integer.toString(TEMPLATE_NAME.hashCode());
    public static final int vmCount = 2;

    public static final String[] extraArgs = new String[] { CLUSTER_NAME, TEMPLATE_NAME, Integer.toString(vmCount) };

    private static final String TEMPLATE_BY_NAME_ADD_COMMAND =
        "$t = select-template -searchtext \"name=" + TEMPLATE_NAME + "\";" +
        "add-vmpool " + "-vmpoolname \"" + NEW_NAME + "\" -vmpooldescription \"" + NEW_DESCRIPTION + "\" -templateid $t.TemplateId -hostclusterid \"" + CLUSTER_ID + "\" -pooltype Automatic";

    private static final String CLUSTER_BY_NAME_ADD_COMMAND =
        "$t = select-template -searchtext \"name=" + TEMPLATE_NAME  + "\";" +
        "$c = select-cluster -searchtext \"name=" + CLUSTER_NAME + "\";" +
        "add-vmpool " + "-vmpoolname \"" + NEW_NAME + "\" -vmpooldescription \"" + NEW_DESCRIPTION + "\" -templateid $t.TemplateId -hostclusterid $c.ClusterId -pooltype Automatic";

    private static final String ADD_COMMAND = "add-vmpool " + "-vmpoolname \"" + NEW_NAME + "\" -vmpooldescription \"" + NEW_DESCRIPTION + "\" -templateid \"" + TEMPLATE_ID + "\" -hostclusterid \"" + CLUSTER_ID + "\" -pooltype Automatic";
    private static final String REMOVE_COMMAND = "$p = get-vmpool -vmpoolid \"" + NAMES[1].hashCode() + "\";remove-vmpool -name $p.name";

    public static final String LOOKUP_CLUSTER_COMMAND = "select-cluster -searchtext \"name = " + CLUSTER_NAME + "\"";
    public static final String LOOKUP_TEMPLATE_COMMAND = "select-template -searchtext \"name = " + TEMPLATE_NAME + "\"";

    public PowerShellVmPoolsResourceTest() {
        super(new PowerShellVmPoolResource("0", null, null, null, null), "vmpools", "vmpool", extraArgs);
    }

    protected String formatCluster(String name) {
        return formatXmlReturn("cluster",
                               new String[] { name },
                               new String[] { "" },
                               PowerShellClustersResourceTest.extraArgs);
    }

    protected String formatTemplate(String name) {
        return formatXmlReturn("template",
                               new String[] { name },
                               new String[] { "" },
                               PowerShellTemplatesResourceTest.extraArgs);
    }

    @Test
    public void testList() throws Exception {
        String [] commands = { getSelectCommand(),
                               LOOKUP_CLUSTER_COMMAND, LOOKUP_TEMPLATE_COMMAND,
                               LOOKUP_CLUSTER_COMMAND, LOOKUP_TEMPLATE_COMMAND,
                               LOOKUP_CLUSTER_COMMAND, LOOKUP_TEMPLATE_COMMAND };
        String [] returns = { getSelectReturn(),
                              formatCluster(CLUSTER_NAME), formatTemplate(TEMPLATE_NAME),
                              formatCluster(CLUSTER_NAME), formatTemplate(TEMPLATE_NAME),
                              formatCluster(CLUSTER_NAME), formatTemplate(TEMPLATE_NAME) };
        resource.setUriInfo(setUpResourceExpectations(4, commands, returns, false, null, NAMES));
        verifyCollection(resource.list().getVmPools(), NAMES, DESCRIPTIONS);
    }


    @Test
    public void testQuery() throws Exception {
        String [] commands = { getQueryCommand(VmPool.class),
                               LOOKUP_CLUSTER_COMMAND, LOOKUP_TEMPLATE_COMMAND,
                               LOOKUP_CLUSTER_COMMAND, LOOKUP_TEMPLATE_COMMAND };
        String [] returns = { getQueryReturn(),
                              formatCluster(CLUSTER_NAME), formatTemplate(TEMPLATE_NAME),
                              formatCluster(CLUSTER_NAME), formatTemplate(TEMPLATE_NAME) };
        resource.setUriInfo(setUpResourceExpectations(3,
                                                      commands,
                                                      returns,
                                                      false,
                                                      getQueryParam(),
                                                      NAMES_SUBSET));
        verifyCollection(resource.list().getVmPools(), NAMES_SUBSET, DESCRIPTIONS_SUBSET);
    }

    @Test
    public void testAdd() throws Exception {
        String [] commands = { ADD_COMMAND, LOOKUP_CLUSTER_COMMAND, LOOKUP_TEMPLATE_COMMAND };
        String [] returns = { getAddReturn(), formatCluster(CLUSTER_NAME), formatTemplate(TEMPLATE_NAME) };
        resource.setUriInfo(setUpResourceExpectations(2, commands, returns, true, null, NEW_NAME));
        verifyResponse(resource.add(getModel(NEW_NAME, NEW_DESCRIPTION)), NEW_NAME, NEW_DESCRIPTION);
    }

    @Test
    public void testAddWithTemplateName() throws Exception {
        VmPool model = getModel(NEW_NAME, NEW_DESCRIPTION);
        model.getTemplate().setId(null);
        model.getTemplate().setName(TEMPLATE_NAME);

        String [] commands = { TEMPLATE_BY_NAME_ADD_COMMAND, LOOKUP_CLUSTER_COMMAND, LOOKUP_TEMPLATE_COMMAND };
        String [] returns = { getAddReturn(), formatCluster(CLUSTER_NAME), formatTemplate(TEMPLATE_NAME) };
        resource.setUriInfo(setUpResourceExpectations(2, commands, returns, true, null, NEW_NAME));
        verifyResponse(resource.add(model), NEW_NAME, NEW_DESCRIPTION);
    }

    @Test
    public void testAddWithClusterName() throws Exception {
        VmPool model = getModel(NEW_NAME, NEW_DESCRIPTION);
        model.getTemplate().setId(null);
        model.getTemplate().setName(TEMPLATE_NAME);
        model.getCluster().setId(null);
        model.getCluster().setName(CLUSTER_NAME);

        String [] commands = { CLUSTER_BY_NAME_ADD_COMMAND, LOOKUP_CLUSTER_COMMAND, LOOKUP_TEMPLATE_COMMAND };
        String [] returns = { getAddReturn(), formatCluster(CLUSTER_NAME), formatTemplate(TEMPLATE_NAME) };
        resource.setUriInfo(setUpResourceExpectations(2, commands, returns, true, null, NEW_NAME));
        verifyResponse(resource.add(model), NEW_NAME, NEW_DESCRIPTION);
    }


    @Test
    public void testAddIncompleteParameters() throws Exception {
        VmPool model = new VmPool();
        model.setName(NEW_NAME);
        model.setTemplate(new Template());
        resource.setUriInfo(setUpResourceExpectations(new String[]{}, new String[]{}, false, null));
        try {
            resource.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "VmPool", "add", "template.id|name", "cluster.id|name");
        }
    }

    @Test
    public void testRemove() throws Exception {
        setUpResourceExpectations(REMOVE_COMMAND, null);
        resource.remove(Integer.toString(NAMES[1].hashCode()));
    }

    @Test
    public void testGetSubResource() throws Exception {
        resource.setUriInfo(setUpResourceExpectations(null, null));
        verifyResource(
            (PowerShellVmPoolResource)resource.getVmPoolSubResource(Integer.toString(NEW_NAME.hashCode())),
            NEW_NAME);
    }

    protected PowerShellVmPoolsResource getResource() {
        return new PowerShellVmPoolsResource();
    }

    protected void populateModel(VmPool pool) {
        Cluster cluster = new Cluster();
        cluster.setId(CLUSTER_ID);
        pool.setCluster(cluster);

        Template template = new Template();
        template.setId(TEMPLATE_ID);
        pool.setTemplate(template);
    }
}

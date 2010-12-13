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
import com.redhat.rhevm.api.model.CPU;
import com.redhat.rhevm.api.model.DataCenter;
import com.redhat.rhevm.api.model.Version;

import org.junit.Test;


public class PowerShellClustersResourceTest extends AbstractPowerShellCollectionResourceTest<Cluster, PowerShellClusterResource, PowerShellClustersResource> {

    private static final String CLUSTER_CPU = "xeon";
    private static final String DATA_CENTER_NAME = "welcometoourimfoverlords";
    private static final String DATA_CENTER_ID = Integer.toString(DATA_CENTER_NAME.hashCode());
    private static final int MAJOR = 10;
    private static final int MINOR = 16;

    public static final String[] extraArgs = new String[] { CLUSTER_CPU, DATA_CENTER_ID, Integer.toString(MAJOR), Integer.toString(MINOR) };

    public PowerShellClustersResourceTest() {
        super(new PowerShellClusterResource("0", null, null, null, null), "clusters", "cluster", extraArgs);
    }

    @Test
    public void testList() throws Exception {
        String [] commands = { getSelectCommand(),
                               getSupportedVersionCommand(NAMES[0]),
                               getSupportedVersionCommand(NAMES[1]),
                               getSupportedVersionCommand(NAMES[2]) };
        String [] returns = { getSelectReturn(),
                              formatVersion(MAJOR, MINOR),
                              formatVersion(MAJOR, MINOR),
                              formatVersion(MAJOR, MINOR) };
        resource.setUriInfo(setUpResourceExpectations(4, commands, returns, false, null, NAMES));
        verifyCollection(resource.list().getClusters(), NAMES, DESCRIPTIONS);
    }


    @Test
    public void testQuery() throws Exception {
        String [] commands = { getQueryCommand(Cluster.class),
                               getSupportedVersionCommand(NAMES[1]),
                               getSupportedVersionCommand(NAMES[2]) };
        String [] returns = { getQueryReturn(),
                              formatVersion(MAJOR, MINOR),
                              formatVersion(MAJOR, MINOR) };
        resource.setUriInfo(setUpResourceExpectations(3,
                                                      commands,
                                                      returns,
                                                      false,
                                                      getQueryParam(),
                                                      NAMES_SUBSET));
        verifyCollection(resource.list().getClusters(), NAMES_SUBSET, DESCRIPTIONS_SUBSET);
    }

    @Test
    public void testAdd() throws Exception {
        String [] commands = { getAddCommand(), getSupportedVersionCommand(NEW_NAME) };
        String [] returns = { getAddReturn(), formatVersion(MAJOR, MINOR) };
        Cluster model = getModel(NEW_NAME, NEW_DESCRIPTION);
        model.setVersion(null);
        resource.setUriInfo(setUpResourceExpectations(2, commands, returns, true, null, NEW_NAME));
        verifyResponse(resource.add(model), NEW_NAME, NEW_DESCRIPTION);
    }

    @Test
    public void testAddWithNamedDataCenter() throws Exception {
        String [] commands = { getAddCommand(true, false), getSupportedVersionCommand(NEW_NAME) };
        String [] returns = { getAddReturn(), formatVersion(MAJOR, MINOR) };
        Cluster model = getModel(NEW_NAME, NEW_DESCRIPTION);
        model.getDataCenter().setId(null);
        model.getDataCenter().setName(DATA_CENTER_NAME);
        model.setVersion(null);
        resource.setUriInfo(setUpResourceExpectations(2, commands, returns, true, null, NEW_NAME));
        verifyResponse(resource.add(model), NEW_NAME, NEW_DESCRIPTION);
    }

    @Test
    public void testAddWithVersion() throws Exception {
        String [] commands = { getAddCommand(false, true), getSupportedVersionCommand(NEW_NAME) };
        String [] returns = { getAddReturn(), formatVersion(MAJOR, MINOR) };
        resource.setUriInfo(setUpResourceExpectations(2, commands, returns, true, null, NEW_NAME));
        verifyResponse(resource.add(getModel(NEW_NAME, NEW_DESCRIPTION)), NEW_NAME, NEW_DESCRIPTION);
    }

    @Test
    public void testAddIncompleteParameters() throws Exception {
        Cluster model = new Cluster();
        model.setName(NEW_NAME);
        resource.setUriInfo(setUpResourceExpectations(new String[]{}, new String[]{}, false, null));
        try {
            resource.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "Cluster", "add", "dataCenter.id|name", "cpu.id");
        }
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
            (PowerShellClusterResource)resource.getClusterSubResource(Integer.toString(NEW_NAME.hashCode())),
            NEW_NAME);
    }

    protected PowerShellClustersResource getResource() {
	return new PowerShellClustersResource();
    }

    protected void populateModel(Cluster cluster) {
        Version version = new Version();
        version.setMajor(MAJOR);
        version.setMinor(MINOR);
        cluster.setVersion(version);

        CPU cpu = new CPU();
        cpu.setId(CLUSTER_CPU);
        cluster.setCpu(cpu);

        DataCenter dataCenter = new DataCenter();
        dataCenter.setId(DATA_CENTER_ID);
        cluster.setDataCenter(dataCenter);
    }

    protected String getAddCommand() {
        return getAddCommand(false, false);
    }

    protected String getAddCommand(boolean withNamedDataCenter, boolean withVersion) {
        StringBuilder buf = new StringBuilder();

        String dataCenterArg = "\"" + DATA_CENTER_ID + "\"";

        if (withNamedDataCenter) {
            buf.append("$d = select-datacenter -searchtext \"name=" + DATA_CENTER_NAME + "\"; ");
            dataCenterArg = "$d.datacenterid";
        }

        buf.append("foreach ($v in get-clustercompatibilityversions -datacenterid " + dataCenterArg + ") {");

        if (withVersion) {
            buf.append(" if ($v.major -eq " + Integer.toString(MAJOR) + " -and $v.minor -eq " + Integer.toString(MINOR) + ") {");
            buf.append(" $version = $v; break } } ");
        } else {
            buf.append(" $version = $v; break } ");
        }

        buf.append("add-cluster");

        buf.append(" -clustername \"" + NEW_NAME + "\"");
        buf.append(" -clusterdescription \"" + NEW_DESCRIPTION + "\"");
        buf.append(" -clustercpuname \"" + CLUSTER_CPU + "\"");
        buf.append(" -datacenterid " + dataCenterArg);
        buf.append(" -compatibilityversion $version");

	return buf.toString();
    }

    protected String getSupportedVersionCommand(String name) {
        StringBuilder buf = new StringBuilder();

        buf.append("get-clustercompatibilityversions -clusterid \"");
        buf.append(Integer.toString(name.hashCode()));
        buf.append("\"");

        return buf.toString();
    }
}

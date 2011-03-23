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

import com.redhat.rhevm.api.model.DataCenter;
import com.redhat.rhevm.api.model.DataCenterStatus;
import com.redhat.rhevm.api.model.StorageType;
import com.redhat.rhevm.api.model.Version;

import com.redhat.rhevm.api.powershell.enums.PowerShellStorageType;

import org.junit.Test;

public class PowerShellDataCentersResourceTest extends AbstractPowerShellCollectionResourceTest<DataCenter, PowerShellDataCenterResource, PowerShellDataCentersResource> {

    private static final String STORAGE_TYPE = Integer.toString(PowerShellStorageType.ISCSI.getValue());
    private static final int MAJOR = 10;
    private static final int MINOR = 16;

    private static final String[] extraArgs = new String[] { STORAGE_TYPE, Integer.toString(MAJOR), Integer.toString(MINOR) };

    public PowerShellDataCentersResourceTest() {
        super(new PowerShellDataCenterResource("0", null, null, null, null), "datacenters", "datacenter", extraArgs);
    }

    @Test
    public void testList() throws Exception {
        String [] commands = { getSelectCommand(),
                               getSupportedVersionCommand(NAMES[0]),
                               getSupportedVersionCommand(NAMES[1]),
                               getSupportedVersionCommand(NAMES[2]) };
        String [] returns =  { getSelectReturn(),
                               formatVersion(MAJOR, MINOR),
                               formatVersion(MAJOR, MINOR),
                               formatVersion(MAJOR, MINOR) };
        resource.setUriInfo(setUpResourceExpectations(4, commands, returns, false, null, NAMES));
        List<DataCenter> datacenters = resource.list().getDataCenters();
        assertEquals(datacenters.get(0).getStatus(), DataCenterStatus.UP);
        verifyCollection(datacenters, NAMES, DESCRIPTIONS);
    }

    @Test
    public void testQuery() throws Exception {
        String [] commands = { getQueryCommand(DataCenter.class),
                               getSupportedVersionCommand(NAMES[1]),
                               getSupportedVersionCommand(NAMES[2]) };
        String [] returns =  { getQueryReturn(),
                               formatVersion(MAJOR, MINOR),
                               formatVersion(MAJOR, MINOR) };
        resource.setUriInfo(setUpResourceExpectations(3, commands, returns, false, getQueryParam(), NAMES_SUBSET));
        verifyCollection(resource.list().getDataCenters(), NAMES_SUBSET, DESCRIPTIONS_SUBSET);
    }

    @Test
    public void testAdd() throws Exception {
        String [] commands = { getAddCommand(),
                               getSupportedVersionCommand(NEW_NAME) };
        String [] returns =  { getAddReturn(),
                               formatVersion(MAJOR, MINOR) };

        DataCenter model = getModel(NEW_NAME, NEW_DESCRIPTION);
        model.setVersion(null);
        resource.setUriInfo(setUpResourceExpectations(2, commands, returns, true, null, NEW_NAME));

        verifyResponse(resource.add(model), NEW_NAME, NEW_DESCRIPTION);
    }

    @Test
    public void testAddIncompleteParameters() throws Exception {
        DataCenter model = new DataCenter();
        model.setName(NEW_NAME);
        resource.setUriInfo(setUpResourceExpectations(new String[]{}, new String[]{}, false, null));
        try {
            resource.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "DataCenter", "add", "storageType");
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
            (PowerShellDataCenterResource)resource.getDataCenterSubResource(Integer.toString(NEW_NAME.hashCode())),
            NEW_NAME);
    }

    protected PowerShellDataCentersResource getResource() {
        return new PowerShellDataCentersResource();
    }

    protected void populateModel(DataCenter dataCenter) {
        Version version = new Version();
        version.setMajor(MAJOR);
        version.setMinor(MINOR);
        dataCenter.setVersion(version);

        dataCenter.setStorageType(StorageType.ISCSI.value());
    }

    protected String getAddCommand() {
        return getAddCommand(false);
    }

    protected String getAddCommand(boolean withVersion) {
        StringBuilder buf = new StringBuilder();

        buf.append("foreach ($v in get-clustercompatibilityversions) {");

        if (withVersion) {
            buf.append(" if ($v.major -eq " + Integer.toString(MAJOR) + " -and $v.minor -eq " + Integer.toString(MINOR) + ") {");
            buf.append(" $version = $v; break } } ");
        } else {
            buf.append(" $version = $v } ");
        }

        buf.append("add-datacenter");

        buf.append(" -name \"" + NEW_NAME + "\"");
        buf.append(" -description \"" + NEW_DESCRIPTION + "\"");
        buf.append(" -datacentertype ISCSI");
        buf.append(" -compatibilityversion $version");

        return buf.toString();
    }

    protected String getCommand(String cmd, String name) {
        StringBuilder buf = new StringBuilder();

        buf.append(cmd);
        buf.append(" -datacenterid \"");
        buf.append(Integer.toString(name.hashCode()));
        buf.append("\"");

        return buf.toString();
    }

    protected String getSupportedVersionCommand(String name) {
        return getCommand("get-datacentercompatibilityversions", name);
    }
}

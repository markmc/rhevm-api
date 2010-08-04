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
import java.util.List;

import com.redhat.rhevm.api.model.DataCenter;
import com.redhat.rhevm.api.model.DataCenters;
import com.redhat.rhevm.api.model.DataCenterStatus;
import com.redhat.rhevm.api.model.StorageType;

import com.redhat.rhevm.api.powershell.enums.PowerShellStorageType;

import org.junit.Test;

public class PowerShellDataCentersResourceTest extends AbstractPowerShellCollectionResourceTest<DataCenter, PowerShellDataCenterResource, PowerShellDataCentersResource> {

    private static final String STORAGE_TYPE = Integer.toString(PowerShellStorageType.ISCSI.getValue());

    private static final String[] extraArgs = new String[] { STORAGE_TYPE };

    private static final String ADD_COMMAND_EPILOG = "-datacentertype ISCSI";

    private static final String GET_STORAGE_COMMAND = "get-storagedomain -datacenterid ";

    public PowerShellDataCentersResourceTest() {
        super(new PowerShellDataCenterResource("0", null, null, null), "datacenters", "datacenter", extraArgs);
    }

    protected String formatStorageDomain(String name) {
        return formatStorageDomain("storagedomain", name);
    }

    protected String formatStorageDomain(String type, String name) {
        return formatXmlReturn(type, new String[] { name }, new String[] { "" }, new String[] {});
    }

    @Test
    public void testList() throws Exception {
        String [] commands = { getSelectCommand(),
                               GET_STORAGE_COMMAND + "\"" + NAMES[0].hashCode() + "\"",
                               GET_STORAGE_COMMAND + "\"" + NAMES[1].hashCode() + "\"",
                               GET_STORAGE_COMMAND + "\"" + NAMES[2].hashCode() + "\"" };
        String [] returns =  { getSelectReturn(),
                               formatStorageDomain("mimas"),
                               formatStorageDomain("dione"),
                               formatStorageDomain("titan") };
         List<DataCenter> datacenters =
             resource.list(setUpResourceExpectations(4, commands, returns, false, null, NAMES)).getDataCenters();
         assertEquals(datacenters.get(0).getStatus(), DataCenterStatus.UP);
         verifyCollection(datacenters, NAMES, DESCRIPTIONS);
    }

    @Test
    public void testList22() throws Exception {
        String [] commands = { getSelectCommand(),
                               GET_STORAGE_COMMAND + "\"" + NAMES[0].hashCode() + "\"",
                               GET_STORAGE_COMMAND + "\"" + NAMES[1].hashCode() + "\"",
                               GET_STORAGE_COMMAND + "\"" + NAMES[2].hashCode() + "\"" };
        String [] returns =  { getSelectReturn(),
                               formatStorageDomain("storagedomain22", "mimas"),
                               formatStorageDomain("storagedomain22", "dione"),
                               formatStorageDomain("storagedomain22", "titan") };
         verifyCollection(
             resource.list(setUpResourceExpectations(4, commands, returns, false, null, NAMES)).getDataCenters(),
             NAMES, DESCRIPTIONS);
    }

    @Test
    public void testQuery() throws Exception {
        String [] commands = { getQueryCommand(DataCenter.class),
                               GET_STORAGE_COMMAND + "\"" + NAMES[1].hashCode() + "\"",
                               GET_STORAGE_COMMAND + "\"" + NAMES[2].hashCode() + "\""};
        String [] returns =  { getQueryReturn(),
                               formatStorageDomain("mimas"),
                               formatStorageDomain("dione") };
         verifyCollection(
             resource.list(setUpResourceExpectations(3, commands, returns, false, getQueryParam(), NAMES_SUBSET)).getDataCenters(),
             NAMES_SUBSET, DESCRIPTIONS_SUBSET);
    }

    @Test
    public void testAdd() throws Exception {
        String [] commands = { getAddCommand() + ADD_COMMAND_EPILOG,
                               GET_STORAGE_COMMAND + "\"" + NEW_NAME.hashCode() + "\""};
        String [] returns =  { getAddReturn(),
                               formatStorageDomain("rhea") };
        verifyResponse(
            resource.add(setUpResourceExpectations(2, commands, returns, true, null, NEW_NAME),
                         getModel(NEW_NAME, NEW_DESCRIPTION)),
            NEW_NAME, NEW_DESCRIPTION);
    }

    @Test
    public void testRemove() throws Exception {
        setUpResourceExpectations(getRemoveCommand(), null);
        resource.remove(Integer.toString(NAMES[1].hashCode()));
    }

    @Test
    public void testGetSubResource() throws Exception {
        verifyResource(
            (PowerShellDataCenterResource)resource.getDataCenterSubResource(setUpResourceExpectations(null, null),
                                                                            Integer.toString(NEW_NAME.hashCode())),
            NEW_NAME);
    }

    protected PowerShellDataCentersResource getResource() {
        return new PowerShellDataCentersResource();
    }

    protected void populateModel(DataCenter dataCenter) {
        dataCenter.setStorageType(StorageType.ISCSI);
    }
}

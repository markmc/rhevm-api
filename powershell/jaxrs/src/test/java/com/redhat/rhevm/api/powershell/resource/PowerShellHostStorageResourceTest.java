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

import java.util.concurrent.Executor;

import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.model.HostStorage;
import com.redhat.rhevm.api.model.Storage;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
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
public class PowerShellHostStorageResourceTest
    extends AbstractPowerShellResourceTest<Host, PowerShellHostResource> {

    private static final String HOST_NAME = "bonkers";
    private static final String[] DEVICES = { "dev0", "dev1", "dev2" };
    private static final String[] VGS = { "vg0", "vg1", "vg2" };

    private static final String GET_STORAGE_DEVICES_CMD = "get-storagedevices -hostid \"" + asId(HOST_NAME) + "\"";
    private static final String GET_STORAGE_DEVICE_CMD = GET_STORAGE_DEVICES_CMD + " | ? { $_.id -eq \"" + asId(DEVICES[0]) + "\" }";
    private static final String GET_STORAGE_ENODEV_CMD = GET_STORAGE_DEVICES_CMD + " | ? { $_.id -eq \"" + asId(VGS[0]) + "\" }";

    private static final String GET_STORAGE_VGS_CMD = "get-storagevolumegroups -hostid \"" + asId(HOST_NAME) + "\"";
    private static final String GET_STORAGE_VG_CMD = GET_STORAGE_VGS_CMD + " | ? { $_.vgid -eq \"" + asId(VGS[0]) + "\" }";

    protected  PowerShellHostResource getResource(Executor executor, PowerShellPoolMap poolMap, PowerShellParser parser, UriInfoProvider uriProvider) {
        return new PowerShellHostResource(asId(HOST_NAME), executor, uriProvider, poolMap, parser);
    }

    protected String formatDevices(String[] names) {
        String[] descriptions = new String[names.length];
        return formatXmlReturn("storagedevice", names, descriptions);
    }

    protected String formatDevice(String name) {
        return formatDevices(asArray(name));
    }

    protected String formatVolumeGroups(String[] names) {
        String[] descriptions = new String[names.length];
        return formatXmlReturn("volumegroup", names, descriptions);
    }

    protected String formatVolumeGroup(String name) {
        return formatVolumeGroups(asArray(name));
    }

    @Test
    public void testGetDevice() {
        PowerShellHostStorageResource parent = new PowerShellHostStorageResource(asId(HOST_NAME), executor, poolMap, parser, uriProvider);
        PowerShellStorageResource resource = new PowerShellStorageResource(asId(DEVICES[0]), parent);

        setUpCmdExpectations(GET_STORAGE_DEVICE_CMD,
                             formatDevice(DEVICES[0]));
        setUriInfo(setUpBasicUriExpectations());
        replayAll();

        verifyStorageDevice(resource.get(), 0);
    }

    @Test
    public void testGetVolumeGroup() {
        PowerShellHostStorageResource parent = new PowerShellHostStorageResource(asId(HOST_NAME), executor, poolMap, parser, uriProvider);
        PowerShellStorageResource resource = new PowerShellStorageResource(asId(VGS[0]), parent);

        String[] commands = { GET_STORAGE_ENODEV_CMD,
                              GET_STORAGE_VG_CMD };
        String[] returns = { "<Objects/>", formatVolumeGroup(VGS[0]) };

        setUpCmdExpectations(commands, returns);
        setUriInfo(setUpBasicUriExpectations());
        replayAll();

        verifyStorageVolumeGroup(resource.get(), 0);
    }

    @Test
    public void testList() {
        PowerShellHostStorageResource resource = new PowerShellHostStorageResource(asId(HOST_NAME), executor, poolMap, parser, uriProvider);

        String[] commands = { GET_STORAGE_DEVICES_CMD,
                              GET_STORAGE_VGS_CMD };
        String[] returns = { formatDevices(DEVICES),
                             formatVolumeGroups(VGS) };

        setUpCmdExpectations(commands, returns);
        setUriInfo(setUpBasicUriExpectations());
        replayAll();

        verifyHostStorage(resource.list());
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

    private void verifyStorageDevice(Storage storage, int i) {
        assertNotNull(storage);
        assertEquals(asId(DEVICES[i]), storage.getId());
        verifyLinks(storage);
    }

    private void verifyStorageVolumeGroup(Storage storage, int i) {
        assertNotNull(storage);
        assertEquals(asId(VGS[i]), storage.getId());
        verifyLinks(storage);
    }

    private void verifyHostStorage(HostStorage hostStorage) {
        assertNotNull(hostStorage);
        assertEquals(DEVICES.length + VGS.length, hostStorage.getStorage().size());
        for (int i = 0; i < DEVICES.length; i++) {
            verifyStorageDevice(hostStorage.getStorage().get(i), i);
        }
        for (int i = 0; i < VGS.length; i++) {
            verifyStorageVolumeGroup(hostStorage.getStorage().get(DEVICES.length + i), i);
        }
    }
}

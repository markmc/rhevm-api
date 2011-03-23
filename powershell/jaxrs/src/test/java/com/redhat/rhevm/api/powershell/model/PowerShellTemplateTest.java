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
package com.redhat.rhevm.api.powershell.model;

import org.junit.Test;

import java.util.List;

import com.redhat.rhevm.api.model.TemplateStatus;
import com.redhat.rhevm.api.model.VmOrigin;
import com.redhat.rhevm.api.model.VmType;

public class PowerShellTemplateTest extends PowerShellModelTest {

    private void testTemplate(PowerShellTemplate t, String id, String name, String description, VmType type, TemplateStatus status, Long memory, int sockets, int cores, String cdIsoPath, String clusterId, String creationTime, VmOrigin origin, String os) {
        assertEquals(id, t.getId());
        assertEquals(name, t.getName());
        assertEquals(description, t.getDescription());
        assertEquals(type.value(), t.getType());
        assertEquals(status, t.getStatus());
        assertEquals(memory, t.getMemory());
        assertNotNull(t.getCpu());
        assertNotNull(t.getCpu().getTopology());
        assertEquals(sockets, t.getCpu().getTopology().getSockets());
        assertEquals(cores, t.getCpu().getTopology().getCores());
        assertEquals(cdIsoPath, t.getCdIsoPath());
        assertNotNull(t.getCluster());
        assertEquals(clusterId, t.getCluster().getId());
        assertEquals(creationTime, t.getCreationTime().toString());
        assertEquals(origin, t.getOrigin());
        assertTrue(t.isSetOs());
        assertTrue(t.getOs().isSetType());
        assertEquals(os, t.getOs().getType());
    }

    @Test
    public void testParse() throws Exception {
        String data = readFileContents("template.xml");
        assertNotNull(data);

        List<PowerShellTemplate> templates = PowerShellTemplate.parse(getParser(), data);

        assertEquals(templates.size(), 4);

        testTemplate(templates.get(0), "00000000-0000-0000-0000-000000000000", "Blank", "Blank template", VmType.DESKTOP, TemplateStatus.OK, 536870912L, 1, 1, null, "99408929-82cf-4dc7-a532-9d998063fa95", "2008-04-01T00:00:00.000Z", VmOrigin.RHEV, "Unassigned");
        testTemplate(templates.get(1), "8d465dcc-df83-4161-868b-ad223744b14a", "foo520", null, VmType.DESKTOP, TemplateStatus.OK, 536870912L, 1, 1, null, "99408929-82cf-4dc7-a532-9d998063fa95", "2010-07-05T18:35:12.000Z", VmOrigin.VMWARE, "OtherLinux");
        testTemplate(templates.get(2), "b3831709-6a0b-41dd-8e96-ea1f2573f95a", "foobar", "foo", VmType.SERVER, TemplateStatus.OK, 536870912L, 1, 1,null, "99408929-82cf-4dc7-a532-9d998063fa95", "2010-07-05T18:33:54.000Z", VmOrigin.XEN, "WindowsXP");
        testTemplate(templates.get(3), "3ee77811-f1eb-4d3f-991e-e539dbb2f1f9", "tmpl1", null, VmType.DESKTOP, TemplateStatus.OK, 536870912L, 1, 1,null, "99408929-82cf-4dc7-a532-9d998063fa95", "2010-07-02T14:37:09.000Z", VmOrigin.RHEV, "OtherLinux");
    }
}

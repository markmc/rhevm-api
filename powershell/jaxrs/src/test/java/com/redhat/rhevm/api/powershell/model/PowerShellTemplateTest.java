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

import java.util.ArrayList;

import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.model.TemplateStatus;


public class PowerShellTemplateTest extends PowerShellModelTest {

    private void testTemplate(PowerShellTemplate t, String id, String name, String description, TemplateStatus status, Long memory, int sockets, int cores, String cdIsoPath, String clusterId) {
        assertEquals(t.getId(), id);
        assertEquals(t.getName(), name);
        assertEquals(t.getDescription(), description);
        assertEquals(t.getStatus(), status);
        assertEquals(t.getMemory(), memory);
        assertNotNull(t.getCpu());
        assertNotNull(t.getCpu().getTopology());
        assertEquals(t.getCpu().getTopology().getSockets(), sockets);
        assertEquals(t.getCpu().getTopology().getCores(), cores);
        assertEquals(t.getCdIsoPath(), cdIsoPath);
        assertNotNull(t.getCluster());
        assertEquals(t.getCluster().getId(), clusterId);
    }

    @Test
    public void testParse() {
        String data = readFileContents("template.data");
        assertNotNull(data);

        ArrayList<PowerShellTemplate> templates = PowerShellTemplate.parse(data);

        assertEquals(templates.size(), 1);

        testTemplate(templates.get(0), "00000000-0000-0000-0000-000000000000", "Blank", "Blank template", TemplateStatus.OK, 536870912L, 1, 1, "foo.iso", "0");
    }
}

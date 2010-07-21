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

import com.redhat.rhevm.api.model.Iso;


public class PowerShellIsoTest extends PowerShellModelTest {

    private void testIso(Iso i, String id) {
        assertEquals(id, i.getId());
        assertEquals(id, i.getName());
    }

    @Test
    public void testParse() throws Exception {
        String data = readFileContents("iso.xml");
        assertNotNull(data);

        List<Iso> isos = PowerShellIso.parse(getParser(), data);

        assertEquals(isos.size(), 3);

        testIso(isos.get(0), "Fedora-13-x86_64-Live.iso");
        testIso(isos.get(1), "en_winxp_pro_with_sp2.iso");
        testIso(isos.get(2), "WindowsXP-sp2-vlk.iso");
    }
}

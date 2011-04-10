/*
 * Copyright © 2011 Red Hat, Inc.
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

import com.redhat.rhevm.api.model.Group;

public class PowerShellGroupTest extends PowerShellModelTest {

    private static final String UID = "031c515e-7d9d-4004-8279-8dd82d83b226";
    private static final String NAME = "Administrators@some_domain.local/Builtin";

    @Test
    public void testParse() throws Exception {
        String data = readFileContents("group.xml");
        assertNotNull(data);

        List<Group> groups = PowerShellGroup.parse(getParser(), data);

        assertEquals(groups.size(), 1);

        verifyGroup(groups.get(0));
    }

    private void verifyGroup(Group u) {
        assertEquals(UID, u.getId());
        assertEquals(NAME, u.getName());
    }
}

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

import com.redhat.rhevm.api.model.Version;

public class PowerShellVersionTest extends PowerShellModelTest {

    private void testVersion(Version v, int major, int minor) {
        assertEquals(major, v.getMajor());
        assertEquals(minor, v.getMinor());
    }

    @Test
    public void testParse() throws Exception {
        String data = readFileContents("version.xml");
        assertNotNull(data);

        List<Version> versions = PowerShellVersion.parse(getParser(), data);

        assertEquals(versions.size(), 1);

        testVersion(versions.get(0), 2, 2);
    }
}

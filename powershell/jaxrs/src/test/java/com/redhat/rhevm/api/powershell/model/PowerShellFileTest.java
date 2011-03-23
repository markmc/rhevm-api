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
import com.redhat.rhevm.api.model.File;
import com.redhat.rhevm.api.model.FileType;

public class PowerShellFileTest extends PowerShellModelTest {

    private void testFile(File i, String id) {
        assertEquals(id, i.getId());
        assertEquals(id, i.getName());
        assertEquals(FileType.ISO.value(), i.getType());
    }

    @Test
    public void testParse() throws Exception {
        String data = readFileContents("iso.xml");
        assertNotNull(data);

        List<File> files = PowerShellFile.parse(getParser(), data);

        assertEquals(files.size(), 3);

        testFile(files.get(0), "Fedora-13-x86_64-Live.iso");
        testFile(files.get(1), "en_winxp_pro_with_sp2.iso");
        testFile(files.get(2), "WindowsXP-sp2-vlk.iso");
    }
}

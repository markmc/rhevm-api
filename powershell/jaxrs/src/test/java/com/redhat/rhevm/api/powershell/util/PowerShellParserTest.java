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
package com.redhat.rhevm.api.powershell.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.io.IOException;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.redhat.rhevm.api.powershell.enums.EnumMapper;

public class PowerShellParserTest extends Assert {

    private String readFileContents(String file) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(file);
        assertNotNull(is);
        try {
            StringBuilder outputBuffer = new StringBuilder();
            Scanner sc = new Scanner(is);
            while (sc.hasNext()) {
                outputBuffer.append(sc.nextLine() + "\n");
            }
            return outputBuffer.toString();
        } finally {
            try {
                is.close();
            } catch (IOException ioe) {
                // ignore
            }
        }
    }

    @Test
    public void testParse() throws Exception {
        String[] files = new String[] {
            "cluster.xml",
            "datacenter.xml",
            "disks.xml",
            "host.xml",
            "network.xml",
            "nics.xml",
            "storagedomain.xml",
            "template.xml",
            "vmpool.xml",
            "vm.xml"
        };

        PowerShellParser p = new PowerShellParser();

        p.setDocumentBuilder(DocumentBuilderFactory.newInstance().newDocumentBuilder());
        p.setEnumMapper(new EnumMapper());

        for (int i = 0; i < files.length; i++) {
            String contents = readFileContents(files[i]);
            p.parse(contents);
        }

    }
}

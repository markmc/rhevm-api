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
import org.junit.Before;
import org.junit.Test;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.redhat.rhevm.api.powershell.enums.EnumMapper;

public class PowerShellParserTest extends Assert {

    private static final String[] FILES = new String[] {
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

    private PowerShellParser parser;

    @Before
    public void setUp() throws Exception {
        parser = new PowerShellParser();

        parser.setDocumentBuilder(DocumentBuilderFactory.newInstance().newDocumentBuilder());
        parser.setEnumMapper(new EnumMapper());
    }

    @Test
    public void testParse() throws Exception {
        for (int i = 0; i < FILES.length; i++) {
            parser.parse(PowerShellTestUtils.readClassPathFile(FILES[i]));
        }
    }

    @Test
    public void testParse22() throws Exception {
        PowerShellParser.Entity cluster =
            parser.parse(PowerShellTestUtils.readClassPathFile("cluster22.xml")).get(0);
        verifyId(cluster, "clusterid", "0");

        PowerShellParser.Entity host =
            parser.parse(PowerShellTestUtils.readClassPathFile("host22.xml")).get(0);
        verifyId(host, "hostid", "1");
        verifyId(host, "hostclusterid", "0");

        PowerShellParser.Entity vmpool =
            parser.parse(PowerShellTestUtils.readClassPathFile("vmpool22.xml")).get(0);
        verifyId(vmpool, "vmpoolid", "2");
    }

    @Test
    public void testConurrentParse()  throws Exception {
        Thread[] threads = new Thread[FILES.length * 10];
        for (int i = 0 ; i < threads.length ; i++) {
            final String file = FILES[i % 10];
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    parser.parse(PowerShellTestUtils.readClassPathFile(file));
                }
            });
        }
        for (int i = 0 ; i < threads.length ; i++) {
            threads[i].start();
        }
        for (int i = 0 ; i < threads.length ; i++) {
            threads[i].join();
        }
    }

    private void verifyId(PowerShellParser.Entity entity, String property, String expected) {
        assertEquals(expected,
                     entity.get(property,
                                String.class,
                                Integer.class).toString());
    }
}

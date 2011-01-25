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
package com.redhat.rhevm.api.common.util;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;

import org.junit.Assert;
import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;


public class DetailHelperTest extends Assert {

    private static final String ACCEPTABLE = "application/xml";

    @Test
    public void testIncludeSingle() throws Exception {
        doTestIncludes(";detail=statistics",
                       new String[] {"statistics", "permissions"},
                       new boolean[] {true, false});
    }

    @Test
    public void testIncludeSome() throws Exception {
        doTestIncludes(";detail=devices ;detail=statistics",
                       new String[] {"statistics", "permissions"},
                       new boolean[] {true, false});
    }

    @Test
    public void testIncludeSomeCollapsed() throws Exception {
        doTestIncludes(";detail=devices+statistics",
                       new String[] {"statistics", "permissions"},
                       new boolean[] {true, false});
    }

    @Test
    public void testIncludeMore() throws Exception {
        doTestIncludes(";detail=devices; detail=statistics; detail=tags; detail=permissions",
                       new String[] {"statistics", "permissions"},
                       new boolean[] {true, true});
    }

    @Test
    public void testIncludeMoreCollapsed() throws Exception {
        doTestIncludes(";detail=devices; detail=statistics+tags+permissions",
                       new String[] {"statistics", "permissions"},
                       new boolean[] {true, true});
    }

    @Test
    public void testIncludeAll() throws Exception {
        doTestIncludes(";detail=statistics; detail=permissions",
                       new String[] {"statistics", "permissions"},
                       new boolean[] {true, true});
    }

    @Test
    public void testIncludeAllCollapsed() throws Exception {
        doTestIncludes(";detail=statistics+permissions",
                       new String[] {"statistics", "permissions"},
                       new boolean[] {true, true});
    }

    @Test
    public void testIncludeWithSpacePrefix() throws Exception {
        doTestIncludes("; detail=statistics ; detail=permissions",
                       new String[] {"statistics", "permissions"},
                       new boolean[] {true, true});
    }

    @Test
    public void testIncludeNone() throws Exception {
        doTestIncludes("",
                       new String[] {"statistics", "permissions"},
                       new boolean[] {false, false});
    }

    private void doTestIncludes(String details, String[] rels, boolean[] expected) throws Exception {

        HttpHeaders httpheaders = createMock(HttpHeaders.class);
        List<String> requestHeaders = new ArrayList<String>();
        expect(httpheaders.getRequestHeader("Accept")).andReturn(requestHeaders).anyTimes();
        requestHeaders.add(ACCEPTABLE + details);

        replay(httpheaders);

        for (int i = 0 ; i < rels.length ; i++) {
            assertEquals(expected[i], DetailHelper.include(httpheaders, rels[i]));
        }

        verify(httpheaders);
    }

}

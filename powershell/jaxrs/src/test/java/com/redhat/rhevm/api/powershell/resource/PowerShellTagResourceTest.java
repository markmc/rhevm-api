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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Fault;
import com.redhat.rhevm.api.model.Tag;

import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;

import org.junit.Test;

import static org.easymock.classextension.EasyMock.expect;

import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;


public class PowerShellTagResourceTest extends AbstractPowerShellResourceTest<Tag, PowerShellTagResource> {

    private static final String TAG_NAME = "foo";
    private static final String TAG_ID = Integer.toString(TAG_NAME.hashCode());
    private static final String TAG_DESCRIPTION = "foo tag";

    private static final String GET_COMMAND = "get-tag \"" + TAG_ID + "\"";
    private static final String UPDATE_COMMAND = "$t = " + GET_COMMAND + "; $t.name = \"eris\"; $t.description = \"\"; update-tag -tagobject $t";

    protected PowerShellTagResource getResource(Executor executor, PowerShellPoolMap poolMap, PowerShellParser parser) {
        return new PowerShellTagResource(TAG_ID, executor, poolMap, parser);
    }

    protected String formatTag(String name, String description) {
        return formatXmlReturn("tag",
                               new String[] { name },
                               new String[] { description },
                               PowerShellTagsResourceTest.extraArgs);
    }

    @Test
    public void testGet() throws Exception {
        verifyTag(
            resource.get(setUpTagExpectations(GET_COMMAND,
                                              formatTag(TAG_NAME, TAG_DESCRIPTION),
                                              TAG_NAME)),
            TAG_NAME, TAG_DESCRIPTION);
    }

    @Test
    public void testGoodUpdate() throws Exception {
        verifyTag(
            resource.update(setUpTagExpectations(UPDATE_COMMAND,
                                                 formatTag("eris", ""),
                                                 "eris"),
                            getTag("eris", "")),
            "eris", null);
    }

    @Test
    public void testBadUpdate() throws Exception {
        try {
            UriInfo uriInfo = createMock(UriInfo.class);
            replayAll();
            resource.update(uriInfo, getTag("98765", "eris", ""));
            fail("expected WebApplicationException on bad update");
        } catch (WebApplicationException wae) {
            verifyUpdateException(wae);
        }
    }

    private UriInfo setUpTagExpectations(String command, String ret, String name) throws Exception {
        mockStatic(PowerShellCmd.class);
        expect(PowerShellCmd.runCommand(setUpPoolExpectations(), command)).andReturn(ret);
        replayAll();
        return null;
    }

    private Tag getTag(String name, String description) {
        return getTag(TAG_ID, name, description);
    }

    private Tag getTag(String id, String name, String description) {
        Tag tag = new Tag();
        tag.setId(id);
        tag.setName(name);
        tag.setDescription(description);
        return tag;
    }

    private void verifyTag(Tag tag, String name, String description) {
        assertNotNull(tag);
        assertEquals(tag.getId(), Integer.toString(name.hashCode()));
        assertEquals(tag.getName(), name);
        assertEquals(tag.getDescription(), description);
    }

    private void verifyUpdateException(WebApplicationException wae) {
        assertEquals(409, wae.getResponse().getStatus());
        Fault fault = (Fault)wae.getResponse().getEntity();
        assertNotNull(fault);
        assertEquals("Broken immutability constraint", fault.getReason());
        assertEquals("Attempt to set immutable field: id", fault.getDetail());
    }
}

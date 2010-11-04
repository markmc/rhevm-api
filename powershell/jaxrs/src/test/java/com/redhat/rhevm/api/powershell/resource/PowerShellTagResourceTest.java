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

import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.model.Fault;
import com.redhat.rhevm.api.model.Tag;
import com.redhat.rhevm.api.model.TagParent;

import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;

import org.junit.Test;

import static org.easymock.EasyMock.expect;

import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;


public class PowerShellTagResourceTest extends AbstractPowerShellResourceTest<Tag, PowerShellTagResource> {

    private static final String TAG_NAME = "foo";
    private static final String TAG_ID = Integer.toString(TAG_NAME.hashCode());
    private static final String TAG_DESCRIPTION = "foo tag";

    private static final String PARENT_NAME = "parent";
    private static final String PARENT_ID = asId(PARENT_NAME);

    private static final String GET_COMMAND = "get-tag \"" + TAG_ID + "\"";
    private static final String UPDATE_COMMAND = "$t = " + GET_COMMAND + "; $t.name = \"eris\"; $t.description = \"\"; $t = update-tag -tagobject $t; $t";
    private static final String MOVE_COMMAND = "$t = " + GET_COMMAND + "; $parent = get-tag \"" + PARENT_ID + "\"; $t = update-tag -tagobject $t; if ($t.parentid -ne $parent.tagid) { move-tag -tagobject $t -parenttagobject $parent } $t";

    protected PowerShellTagResource getResource(Executor executor, PowerShellPoolMap poolMap, PowerShellParser parser, UriInfoProvider uriProvider) {
        return new PowerShellTagResource(TAG_ID, executor, uriProvider, poolMap, parser);
    }

    protected String formatTag(String name, String description) {
        return formatXmlReturn("tag",
                               new String[] { name },
                               new String[] { description },
                               PowerShellTagsResourceTest.extraArgs);
    }

    @Test
    public void testGet() throws Exception {
        setUriInfo(setUpTagExpectations(GET_COMMAND,
                                        formatTag(TAG_NAME, TAG_DESCRIPTION),
                                        TAG_NAME));
        verifyTag(resource.get(), TAG_NAME, TAG_DESCRIPTION);
    }

    @Test
    public void testGoodUpdate() throws Exception {
        setUriInfo(setUpTagExpectations(UPDATE_COMMAND,
                                        formatTag("eris", ""),
                                        "eris"));
        verifyTag(resource.update(getTag("eris", "")), "eris", null);
    }

    @Test
    public void testMove() throws Exception {
        Tag tag = new Tag();
        tag.setParent(new TagParent());
        tag.getParent().setTag(new Tag());
        tag.getParent().getTag().setId(PARENT_ID);

        setUriInfo(setUpTagExpectations(MOVE_COMMAND,
                                        formatTag("eris", ""),
                                        "eris"));
        verifyTag(resource.update(tag), "eris", null);
    }

    @Test
    public void testBadUpdate() throws Exception {
        try {
            setUriInfo(createMock(UriInfo.class));
            replayAll();
            resource.update(getTag("98765", "eris", ""));
            fail("expected WebApplicationException on bad update");
        } catch (WebApplicationException wae) {
            verifyUpdateException(wae);
        }
    }

    private UriInfo setUpTagExpectations(String command, String ret, String name) throws Exception {
        mockStatic(PowerShellCmd.class);
        expect(PowerShellCmd.runCommand(setUpPoolExpectations(), command)).andReturn(ret);
        UriInfo uriInfo = setUpBasicUriExpectations();
        replayAll();
        return uriInfo;
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
        verifyLinks(tag);
    }

    private void verifyUpdateException(WebApplicationException wae) {
        assertEquals(409, wae.getResponse().getStatus());
        Fault fault = (Fault)wae.getResponse().getEntity();
        assertNotNull(fault);
        assertEquals("Broken immutability constraint", fault.getReason());
        assertEquals("Attempt to set immutable field: id", fault.getDetail());
    }
}

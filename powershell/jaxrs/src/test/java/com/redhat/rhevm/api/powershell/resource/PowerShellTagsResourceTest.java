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

import java.util.List;

import javax.ws.rs.WebApplicationException;

import com.redhat.rhevm.api.model.Tag;

import org.junit.Test;

public class PowerShellTagsResourceTest
    extends AbstractPowerShellCollectionResourceTest<Tag, PowerShellTagResource, PowerShellTagsResource> {

    public static final String[] extraArgs = new String[]{};

    public PowerShellTagsResourceTest() {
        super(new PowerShellTagResource("0", null, null, null), "tags", "tag", extraArgs);
    }

    @Test
    public void testList() throws Exception {
        setUpResourceExpectations("get-tags", getSelectReturn(), NAMES);
        List<Tag> tags = resource.list().getTags();
        verifyCollection(tags, NAMES, DESCRIPTIONS);
    }

    @Test
    public void testAdd() throws Exception {
        verifyResponse(
            resource.add(setUpResourceExpectations(asArray(getAddCommand()), asArray(getAddReturn()), true, null, NEW_NAME),
                         getModel(NEW_NAME, NEW_DESCRIPTION)),
            NEW_NAME, NEW_DESCRIPTION);
    }

    @Test
    public void testAddIncompleteParameters() throws Exception {
        try {
            resource.add(setUpResourceExpectations(new String[]{}, new String[]{}, false, null), new Tag());
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "Tag", "add", "name");
        }
    }

    @Test
    public void testRemove() throws Exception {
        setUpResourceExpectations(getRemoveCommand(), null);
        resource.remove(Integer.toString(NAMES[1].hashCode()));
    }

    @Test
    public void testGetSubResource() throws Exception {
        verifyResource(
            (PowerShellTagResource)resource.getTagSubResource(Integer.toString(NEW_NAME.hashCode())),
            NEW_NAME);
    }

    protected PowerShellTagsResource getResource() {
        return new PowerShellTagsResource();
    }

    protected void populateModel(Tag tag) {
    }
}

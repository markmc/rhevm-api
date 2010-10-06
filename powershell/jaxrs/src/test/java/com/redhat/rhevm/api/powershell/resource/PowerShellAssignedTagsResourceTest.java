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

import java.net.URI;
import java.util.concurrent.Executor;

import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.UriBuilder;

import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.model.Tag;
import com.redhat.rhevm.api.model.Tags;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPool;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;

import org.junit.Test;

import org.junit.runner.RunWith;

import static org.easymock.classextension.EasyMock.expect;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { PowerShellCmd.class })
public class PowerShellAssignedTagsResourceTest
    extends AbstractPowerShellResourceTest<VM, PowerShellVmResource> {

    private static final String VM_NAME = "MadDog";
    private static final String[] TAGS = { "zig", "zag", "zog" };

    private static final String[] extraArgs = new String[] { };

    private static final String GET_VM_TAGS_CMD = "get-tags -vmid \"" + asId(VM_NAME) + "\"";

    private static final String GET_VM_TAG_CMD = "get-tag \"" + asId(TAGS[0]) + "\"";

    private static final String ADD_VM_TAG_CMD = "$tag = get-tag \"" + asId(TAGS[0]) +"\"; attach-tag -tagobject $tag -vmid \"" + asId(VM_NAME) + "\"";
    private static final String ADD_VM_TAG_BY_NAME_CMD = "$tag = get-tags | ? { $_.name -eq \"" + TAGS[0] +"\" }; attach-tag -tagobject $tag -vmid \"" + asId(VM_NAME) + "\"";
    private static final String REMOVE_VM_TAG_CMD = "$tag = get-tag \"" + asId(TAGS[0]) +"\"; detach-tag -tagobject $tag -vmid \"" + asId(VM_NAME) + "\"";

    protected  PowerShellVmResource getResource(Executor executor, PowerShellPoolMap poolMap, PowerShellParser parser, UriInfoProvider uriProvider) {
        return new PowerShellVmResource(asId(VM_NAME), executor, uriProvider, poolMap, parser);
    }

    protected String formatTags(String[] names, String[] args) {
        String[] descriptions = new String[names.length];
        return formatXmlReturn("tag", names, descriptions, args);
    }

    protected String formatTag(String name, String[] args) {
        return formatTags(asArray(name), args);
    }

    @Test
    public void testVmTagGet() {
        PowerShellAssignedTagsResource parent = new PowerShellAssignedTagsResource(VM.class, asId(VM_NAME), poolMap, parser, uriProvider);
        PowerShellAssignedTagResource tagResource = new PowerShellAssignedTagResource(asId(TAGS[0]), parent);

        setUpCmdExpectations(GET_VM_TAG_CMD,
                             formatTag(TAGS[0], extraArgs));
        setUriInfo(setUpBasicUriExpectations());
        replayAll();

        verifyTag(tagResource.get(), 0);
    }

    @Test
    public void testVmTagsList() {
        PowerShellAssignedTagsResource tagResource = new PowerShellAssignedTagsResource(VM.class, asId(VM_NAME), poolMap, parser, uriProvider);

        setUpCmdExpectations(GET_VM_TAGS_CMD, formatTags(TAGS, extraArgs));
        setUriInfo(setUpBasicUriExpectations());
        replayAll();

        verifyTags(tagResource.list());
    }

    @Test
    public void testVmTagAdd() throws Exception {
        PowerShellAssignedTagsResource tagResource = new PowerShellAssignedTagsResource(VM.class, asId(VM_NAME),  poolMap, parser, uriProvider);

        setUpCmdExpectations(ADD_VM_TAG_CMD, formatTag(TAGS[0], extraArgs));
        setUriInfo(setUpUriInfoExpections(asId(TAGS[0])));
        replayAll();

        Tag tag = new Tag();
        tag.setId(asId(TAGS[0]));

        verifyTag((Tag)tagResource.add(tag).getEntity(), 0);
    }

    @Test
    public void testVmTagAddByName() throws Exception {
        PowerShellAssignedTagsResource tagResource = new PowerShellAssignedTagsResource(VM.class, asId(VM_NAME), poolMap, parser, uriProvider);

        setUpCmdExpectations(ADD_VM_TAG_BY_NAME_CMD, formatTag(TAGS[0], extraArgs));
        setUriInfo(setUpUriInfoExpections(asId(TAGS[0])));
        replayAll();

        Tag tag = new Tag();
        tag.setName(TAGS[0]);

        verifyTag((Tag)tagResource.add(tag).getEntity(), 0);
    }

    @Test
    public void testRemove() {
        PowerShellAssignedTagsResource resource = new PowerShellAssignedTagsResource(VM.class, asId(VM_NAME), poolMap, parser, uriProvider);

        setUpCmdExpectations(REMOVE_VM_TAG_CMD, "");
        replayAll();

        resource.remove(asId(TAGS[0]));
    }

    private UriInfo setUpUriInfoExpections(String id) throws Exception {
        UriInfo uriInfo = setUpBasicUriExpectations();
        UriBuilder uriBuilder = createMock(UriBuilder.class);
        expect(uriInfo.getAbsolutePathBuilder()).andReturn(uriBuilder);
        expect(uriBuilder.path(id)).andReturn(uriBuilder);
        expect(uriBuilder.build()).andReturn(new URI("vms/" + asId(VM_NAME) + "/tags/" + id)).anyTimes();
        return uriInfo;
    }

    private void setUpCmdExpectations(String command, String ret) {
        mockStatic(PowerShellCmd.class);
        expect(PowerShellCmd.runCommand(setUpPoolExpectations(), command)).andReturn(ret);
    }

    protected PowerShellPool setUpPoolExpectations() {
        PowerShellPool pool = createMock(PowerShellPool.class);
        expect(poolMap.get()).andReturn(pool);
        return pool;
    }

    private void verifyTag(Tag tag, int i) {
        assertNotNull(tag);
        assertEquals(asId(TAGS[i]), tag.getId());
        assertEquals(TAGS[i], tag.getName());
        verifyLinks(tag);
    }

    private void verifyTags(Tags tags) {
        assertNotNull(tags);
        assertEquals(TAGS.length, tags.getTags().size());
        for (int i = 0; i < TAGS.length; i++) {
            verifyTag(tags.getTags().get(i), i);
        }
    }
}

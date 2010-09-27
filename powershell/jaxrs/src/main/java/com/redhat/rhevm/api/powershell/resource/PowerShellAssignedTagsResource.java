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

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.Executor;

import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.resource.MediaType;

import com.redhat.rhevm.api.model.BaseResource;
import com.redhat.rhevm.api.model.Tag;
import com.redhat.rhevm.api.model.Tags;
import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.common.util.ReflectionHelper;
import com.redhat.rhevm.api.powershell.model.PowerShellTag;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPool;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
import com.redhat.rhevm.api.resource.AssignedTagResource;
import com.redhat.rhevm.api.resource.AssignedTagsResource;

import static com.redhat.rhevm.api.common.util.CompletenessAssertor.validateParameters;

@Produces(MediaType.APPLICATION_XML)
public class PowerShellAssignedTagsResource
    extends AbstractPowerShellResource
    implements AssignedTagsResource {

    protected Class<? extends BaseResource> parentType;
    protected String parentId;

    public PowerShellAssignedTagsResource(Class<? extends BaseResource> parentType,
                                          String parentId,
                                          PowerShellPoolMap shellPools,
                                          PowerShellParser parser) {
        super(shellPools, parser);
        this.parentType = parentType;
        this.parentId = parentId;
    }

    public Class<? extends BaseResource> getParentType() {
        return parentType;
    }

    public String getParentId() {
        return parentId;
    }

    public List<Tag> runAndParse(String command) {
        return PowerShellTag.parse(getParser(), PowerShellCmd.runCommand(getPool(), command));
    }

    public Tag runAndParseSingle(String command) {
        List<Tag> tags = runAndParse(command);
        return !tags.isEmpty() ? tags.get(0) : null;
    }

    public Tag addLinks(Tag tag) {
        ReflectionHelper.assignChildModel(tag, parentType).setId(parentId);

        return LinkHelper.addLinks(tag);
    }

    private String getParentIdArg() {
        return MessageFormat.format("-{0}id {1}",
                                    parentType.getSimpleName().toLowerCase(),
                                    PowerShellUtils.escape(parentId));
    }

    public Tag getTag(String id) {
        return runAndParseSingle("get-tag " + PowerShellUtils.escape(id));
    }

    @Override
    public Tags list() {
        Tags ret = new Tags();
        for (Tag tag : runAndParse("get-tags " + getParentIdArg())) {
            ret.getTags().add(addLinks(tag));
        }
        return ret;
    }

    @Override
    public Response add(UriInfo uriInfo, Tag tag) {
        validateParameters(tag, "id|name");

        StringBuilder buf = new StringBuilder();

        if (tag.isSetId()) {
            buf.append("$tag = get-tag " + PowerShellUtils.escape(tag.getId()) + "; ");
        } else {
            buf.append("$tag = get-tags | ? { $_.name -eq " + PowerShellUtils.escape(tag.getName()) + " }; ");
        }

        buf.append("attach-tag -tagobject $tag " + getParentIdArg());

        tag = addLinks(runAndParseSingle(buf.toString()));

        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(tag.getId());

        return Response.created(uriBuilder.build()).entity(tag).build();
    }

    @Override
    public void remove(String id) {
        StringBuilder buf = new StringBuilder();

        buf.append("$tag = get-tag " + PowerShellUtils.escape(id) + "; ");
        buf.append("detach-tag -tagobject $tag " + getParentIdArg());

        PowerShellCmd.runCommand(getPool(), buf.toString());
    }

    @Override
    public AssignedTagResource getAssignedTagSubResource(String id) {
        return new PowerShellAssignedTagResource(id, this);
    }
}

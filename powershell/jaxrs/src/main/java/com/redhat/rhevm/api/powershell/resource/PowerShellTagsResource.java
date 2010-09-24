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

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.model.Tag;
import com.redhat.rhevm.api.model.Tags;
import com.redhat.rhevm.api.resource.TagResource;
import com.redhat.rhevm.api.resource.TagsResource;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;

import static com.redhat.rhevm.api.common.util.CompletenessAssertor.validateParameters;

public class PowerShellTagsResource
    extends AbstractPowerShellCollectionResource<Tag, PowerShellTagResource>
    implements TagsResource {

    public List<Tag> runAndParse(String command) {
        return PowerShellTagResource.runAndParse(getPool(), getParser(), command);
    }

    public Tag runAndParseSingle(String command) {
        return PowerShellTagResource.runAndParseSingle(getPool(), getParser(), command);
    }

    @Override
    public Tags list() {
        Tags ret = new Tags();
        for (Tag tag : runAndParse("get-tags")) {
            ret.getTags().add(LinkHelper.addLinks(tag));
        }
        return ret;
    }

    @Override
    public Response add(UriInfo uriInfo, Tag tag) {
        validateParameters(tag, "name");

        StringBuilder buf = new StringBuilder();

        buf.append("add-tag");
        buf.append(" -name " + PowerShellUtils.escape(tag.getName()));
        if (tag.isSetDescription()) {
            buf.append(" -description " + PowerShellUtils.escape(tag.getDescription()));
        }

        tag = LinkHelper.addLinks(runAndParseSingle(buf.toString()));

        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(tag.getId());

        return Response.created(uriBuilder.build()).entity(tag).build();
    }

    @Override
    public void remove(String id) {
        PowerShellCmd.runCommand(getPool(), "remove-tag -tagid " + PowerShellUtils.escape(id));
        removeSubResource(id);
    }

    @Override
    public TagResource getTagSubResource(String id) {
        return getSubResource(id);
    }

    protected PowerShellTagResource createSubResource(String id) {
        return new PowerShellTagResource(id, getExecutor(), shellPools, getParser());
    }
}

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
import java.util.concurrent.Executor;


import com.redhat.rhevm.api.model.Tag;
import com.redhat.rhevm.api.resource.TagResource;
import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.powershell.model.PowerShellTag;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPool;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;

public class PowerShellTagResource extends AbstractPowerShellActionableResource<Tag> implements TagResource {

    public PowerShellTagResource(String id,
                                 Executor executor,
                                 UriInfoProvider uriProvider,
                                 PowerShellPoolMap shellPools,
                                 PowerShellParser parser) {
        super(id, executor, uriProvider, shellPools, parser);
    }

    public static List<Tag> runAndParse(PowerShellPool pool, PowerShellParser parser, String command) {
        return PowerShellTag.parse(parser, PowerShellCmd.runCommand(pool, command));
    }

    public static Tag runAndParseSingle(PowerShellPool pool, PowerShellParser parser, String command) {
        List<Tag> tags = runAndParse(pool, parser, command);

        return !tags.isEmpty() ? tags.get(0) : null;
    }

    public List<Tag> runAndParse(String command) {
        return runAndParse(getPool(), getParser(), command);
    }

    public Tag runAndParseSingle(String command) {
        return runAndParseSingle(getPool(), getParser(), command);
    }

    @Override
    public Tag get() {
        return LinkHelper.addLinks(getUriInfo(), runAndParseSingle("get-tag " + PowerShellUtils.escape(getId())));
    }

    @Override
    public Tag update(Tag tag) {
        validateUpdate(tag);

        String parentId = null;
        if (tag.isSetParent() && tag.getParent().isSetTag() && tag.getParent().getTag().isSetId()) {
            parentId = tag.getParent().getTag().getId();
        }

        StringBuilder buf = new StringBuilder();

        buf.append("$t = get-tag " + PowerShellUtils.escape(getId()) + "; ");

        if (parentId != null) {
            buf.append("$parent = get-tag " + PowerShellUtils.escape(parentId) + "; ");
        }

        if (tag.getName() != null) {
            buf.append("$t.name = " + PowerShellUtils.escape(tag.getName()) + "; ");
        }
        if (tag.getDescription() != null) {
            buf.append("$t.description = " + PowerShellUtils.escape(tag.getDescription()) + "; ");
        }

        buf.append("$t = update-tag -tagobject $t; ");

        if (parentId != null) {
            buf.append("if ($t.parentid -ne $parent.tagid) { ");
            buf.append("move-tag -tagobject $t -parenttagobject $parent");
            buf.append(" } ");
        }

        buf.append("$t");

        return LinkHelper.addLinks(getUriInfo(), runAndParseSingle(buf.toString()));
    }
}

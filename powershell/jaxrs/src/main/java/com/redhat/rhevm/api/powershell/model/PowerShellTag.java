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
package com.redhat.rhevm.api.powershell.model;

import java.util.ArrayList;
import java.util.List;

import com.redhat.rhevm.api.model.Tag;
import com.redhat.rhevm.api.model.TagParent;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;

public class PowerShellTag {

    public static List<Tag> parse(PowerShellParser parser, String output) {
        List<Tag> ret = new ArrayList<Tag>();

        for (PowerShellParser.Entity entity : parser.parse(output)) {
            Tag tag = new Tag();

            tag.setId(entity.get("tagid", String.class, Integer.class).toString());
            tag.setName(entity.get("name"));
            tag.setDescription(entity.get("description"));

            tag.setParent(new TagParent());
            tag.getParent().setTag(new Tag());
            tag.getParent().getTag().setId(entity.get("parentid", String.class, Integer.class).toString());

            ret.add(tag);
        }

        return ret;
    }
}

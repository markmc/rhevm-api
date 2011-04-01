/*
 * Copyright © 2011 Red Hat, Inc.
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

import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.redhat.rhevm.api.model.HostState;

public class HostQueryTransform implements QueryTransform {

    private static final String STATES_PATTERN = "(" + join(HostState.values(), "|") + ")";

    private static final Pattern PATTERN = Pattern.compile("(?i:(status\\W+)" + STATES_PATTERN + ")");

    private Map<HostState, String> map = new EnumMap<HostState, String>(HostState.class);

    {
        map.put(HostState.ACTIVE, "up");
        map.put(HostState.INACTIVE, "maintenance");
        map.put(HostState.PENDING_APPROVAL, "pendingapproval");
    }

    public String transform(String query) {
        StringBuffer buf = new StringBuffer();

        Matcher m = PATTERN.matcher(query);

        while (m.find()) {
            HostState state = HostState.valueOf(m.group(2).toUpperCase());
            m.appendReplacement(buf, "$1" + (map.containsKey(state) ? map.get(state) : m.group(2)));
        }
        m.appendTail(buf);

        return buf.toString();
    }

    private static String join(Enum<?>[] values, String delim) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            buf.append(values[i].name());
            if (i + 1 < values.length) {
                buf.append(delim);
            }
        }
        return buf.toString();
    }
}

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

import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.model.Event;
import com.redhat.rhevm.api.model.Events;
import com.redhat.rhevm.api.powershell.model.PowerShellEvent;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
import com.redhat.rhevm.api.resource.EventResource;
import com.redhat.rhevm.api.resource.EventsResource;

public class PowerShellEventsResource extends InjectableUriProviderBase implements EventsResource {

    protected List<Event> runAndParse(String command) {
        return PowerShellEvent.parse(getParser(), PowerShellCmd.runCommand(getPool(), command));
    }

    protected Event runAndParseSingle(String command) {
        List<Event> events = runAndParse(command);
        return !events.isEmpty() ? events.get(0) : null;
    }

    @Override
    public Events list() {
        Events ret = new Events();
        for (Event event : runAndParse(getSelectCommand("select-event", getUriInfo(), Event.class))) {
            ret.getEvent().add(LinkHelper.addLinks(getUriInfo(), event));
        }
        return ret;
    }

    @Override
    public EventResource getEventSubResource(String id) {
        return new PowerShellEventResource(id, this);
    }

    Event lookupEvent(String id) {
        StringBuilder buf = new StringBuilder();
        buf.append("select-event");
        buf.append(" | ? ");
        buf.append("{ $_.id -eq ");
        buf.append(PowerShellUtils.escape(id));
        buf.append(" }");
        return LinkHelper.addLinks(getUriInfo(), runAndParseSingle(buf.toString()));
    }
}

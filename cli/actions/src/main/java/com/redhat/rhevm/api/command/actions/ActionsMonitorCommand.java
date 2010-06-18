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
package com.redhat.rhevm.api.command.actions;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;

import com.redhat.rhevm.api.command.base.AbstractCommand;

import com.redhat.rhevm.api.model.Action;

/**
 * Displays the status of an Action
 */
@Command(scope = "actions", name = "monitor", description = "Report status of an asynchronous action.")
public class ActionsMonitorCommand extends AbstractCommand {

    @Argument(index = 0, name = "url", description = "URL of the action to monitor", required = true, multiValued = false)
    protected String url;

    protected Object doExecute() throws Exception {
        Action action = client.get(url, Action.class);
        System.out.print("[" + client.getLink(action.getLink(), "replay"));
        System.out.println("] status: " + action.getStatus());
        return null;
    }
}

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
import org.apache.felix.gogo.commands.Option;

import com.redhat.rhevm.api.command.base.AbstractCommand;

import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.Status;

public abstract class AbstractActionsCommand extends AbstractCommand {

    @Argument(index = 0, name = "url", description = "URL of the action of interest", required = true, multiValued = false)
    protected String url;

    @Option(name = "-d", aliases = { "--detail" }, description = "Display Fault detail", required = false, multiValued = false)
    protected boolean detail;

    protected void display(Action action) throws Exception {
        System.out.print("[" + client.getLink(action.getLink(), "replay"));
        System.out.println("] status: " + action.getStatus());
        if (Status.FAILED.equals(action.getStatus()) && action.isSetFault()) {
            System.out.println("reason: [" + action.getFault().getReason() + "]");
            if (detail) {
                System.out.println("detail: [" + action.getFault().getDetail() + "]");
            }
        }
    }
}

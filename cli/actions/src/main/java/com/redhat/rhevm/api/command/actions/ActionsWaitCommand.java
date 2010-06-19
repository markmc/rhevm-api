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

import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;

import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.Status;

/**
 * Awaits the completion of an Action
 */
@Command(scope = "actions", name = "wait", description = "Await the completion of an asynchronous action.")
public class ActionsWaitCommand extends AbstractActionsCommand {

    @Option(name = "-b", aliases = { "--bound" }, description = "Maximum time towait (in seconds)", required = false, multiValued = false)
    protected int bound = -1;

    @Option(name = "-p", aliases = { "--period" }, description = "Polling period (in seconds)", required = false, multiValued = false)
    protected int period = 1;

    protected Object doExecute() throws Exception {
        long start = System.currentTimeMillis();
        Action action = null;
        do {
            action = client.get(url, Action.class);
        } while (ongoing(action) && moreTime(start));
        display(action);
        return null;
    }

    private boolean ongoing(Action action) {
        return !Status.COMPLETE.equals(action.getStatus());
    }

    private boolean moreTime(long start) {
        boolean moreTime = bound == -1 || System.currentTimeMillis() - start < (bound * 1000);
        if (moreTime) {
            sleep();
        }
        return moreTime;
    }

    private void sleep() {
        try {
            Thread.sleep(period * 1000);
        } catch (Exception e) {
            // ignore
        }
    }
}

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
package com.redhat.rhevm.api.command.base;

import java.util.List;

import org.apache.felix.gogo.commands.Option;
import com.redhat.rhevm.api.command.base.AbstractCommand;
import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.GracePeriod;
import com.redhat.rhevm.api.model.Link;
import com.redhat.rhevm.api.model.BaseResource;

/**
 * Performs an action on a resource
 */
public abstract class AbstractActionCommand extends AbstractCommand {

    @Option(name = "-a", aliases = { "--async" }, description = "request that the action is processed asynchronously", required = false, multiValued = false)
    private boolean async;

    @Option(name = "-g", aliases = {"--grace"}, description="Grace period to wait before initiating action", required = false, multiValued = false)
    protected long grace = -1L;

    @Option(name = "-d", aliases = { "--detail" }, description = "Display Fault detail", required = false, multiValued = false)
    protected boolean detail;

    protected void doAction(List<? extends BaseResource> collection, String verb, Action action, String name) throws Exception {
        applyOptions(action);
        BaseResource resource = getResource(collection, name);
        if (resource != null) {
            Link link = getLink(resource, verb);
            if (link != null) {
                client.doAction(verb, action, link, detail);
                return;
            }
        }
    }

    private void applyOptions(Action action) {
        action.setAsync(async);
        if (grace != -1) {
            GracePeriod gracePeriod = new GracePeriod();
            gracePeriod.setExpiry(grace);
            action.setGracePeriod(gracePeriod);
        }
    }
}

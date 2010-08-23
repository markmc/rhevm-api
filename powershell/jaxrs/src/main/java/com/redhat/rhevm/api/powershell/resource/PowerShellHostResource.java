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

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.model.Link;
import com.redhat.rhevm.api.resource.HostResource;
import com.redhat.rhevm.api.resource.HostNicsResource;
import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.powershell.model.PowerShellHost;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellPool;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;

import static com.redhat.rhevm.api.common.util.CompletenessAssertor.validateParameters;


public class PowerShellHostResource extends AbstractPowerShellActionableResource<Host> implements HostResource {

    public PowerShellHostResource(String id,
                                  Executor executor,
                                  PowerShellPoolMap shellPools,
                                  PowerShellParser parser) {
        super(id, executor, shellPools, parser);
    }

    /* needed because there are two get-host commands */
    private static final String CMD_PREFIX = "rhevmpssnapin\\";

    public static List<Host> runAndParse(PowerShellPool pool, PowerShellParser parser, String command) {
        return PowerShellHost.parse(parser, PowerShellCmd.runCommand(pool, command));
    }

    public static Host runAndParseSingle(PowerShellPool pool, PowerShellParser parser, String command) {
        List<Host> hosts = runAndParse(pool, parser, command);

        return !hosts.isEmpty() ? hosts.get(0) : null;
    }

    public List<Host> runAndParse(String command) {
        return runAndParse(getPool(), getParser(), command);
    }

    public Host runAndParseSingle(String command) {
        return runAndParseSingle(getPool(), getParser(), command);
    }

    public static Host addLinks(Host host) {
        Link link = new Link();
        link.setRel("nics");
        link.setHref(LinkHelper.getUriBuilder(host).path("nics").build().toString());

        host.getLinks().clear();
        host.getLinks().add(link);

        return LinkHelper.addLinks(host);
    }

    @Override
    public Host get(UriInfo uriInfo) {
        return addLinks(runAndParseSingle(CMD_PREFIX + "get-host " + PowerShellUtils.escape(getId())));
    }

    @Override
    public Host update(UriInfo uriInfo, Host host) {
        validateUpdate(host);

        StringBuilder buf = new StringBuilder();

        buf.append("$h = " + CMD_PREFIX + "get-host " + PowerShellUtils.escape(getId()) + ";");

        if (host.getName() != null) {
            buf.append("$h.name = " + PowerShellUtils.escape(host.getName()) + ";");
        }

        buf.append("update-host -hostobject $h");

        return addLinks(runAndParseSingle(buf.toString()));
    }

    @Override
    public Response approve(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, new CommandRunner(action, "approve-host", "host", getId(), getPool()));
    }

    @Override
    public Response install(UriInfo uriInfo, Action action) {
        validateParameters(action, "rootPassword");
        return doAction(uriInfo, new HostInstaller(action, action.getRootPassword(), getPool()));
    }

    @Override
    public Response activate(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, new CommandRunner(action, "resume-host", "host", getId(), getPool()));
    }

    @Override
    public Response deactivate(UriInfo uriInfo, Action action) {
        return doAction(uriInfo, new CommandRunner(action, "suspend-host", "host", getId(), getPool()));
    }

    @Override
    public Response commitNetConfig(UriInfo uriInfo, Action action) {
        StringBuilder buf = new StringBuilder();

        buf.append("$h = get-host " + PowerShellUtils.escape(getId()) + "; ");
        buf.append("commit-configurationchanges");
        buf.append(" -hostobject $h");

        return doAction(uriInfo, new CommandRunner(action, buf.toString(), getPool()));
    }

    class HostInstaller extends AbstractPowerShellActionTask {

        private String rootPassword;
        private PowerShellPool pool;

        HostInstaller(Action action, String rootPassword, PowerShellPool pool) {
            super(action, "$h = " + CMD_PREFIX + "get-host ");
            this.rootPassword = rootPassword;
            this.pool = pool;
        }

        public void execute() {
            String id = PowerShellHostResource.this.getId();
            StringBuilder buf = new StringBuilder();

            buf.append(command + PowerShellUtils.escape(id) + ";");

            buf.append("update-host");
            buf.append(" -hostobject $h");
            buf.append(" -install");
            buf.append(" -rootpassword " + PowerShellUtils.escape(rootPassword));

            PowerShellCmd.runCommand(pool, buf.toString());
        }
    }

    @Override
    public HostNicsResource getHostNicsResource() {
        return new PowerShellHostNicsResource(getId(), getExecutor(), shellPools, getParser());
    }
}

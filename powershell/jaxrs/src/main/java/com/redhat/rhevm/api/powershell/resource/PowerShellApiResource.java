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

import java.util.concurrent.Executor;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.common.util.JAXBHelper;
import com.redhat.rhevm.api.model.API;
import com.redhat.rhevm.api.model.ApiSummary;
import com.redhat.rhevm.api.model.Hosts;
import com.redhat.rhevm.api.model.Link;
import com.redhat.rhevm.api.model.LinkHeader;
import com.redhat.rhevm.api.model.ObjectFactory;
import com.redhat.rhevm.api.model.StorageDomains;
import com.redhat.rhevm.api.model.Users;
import com.redhat.rhevm.api.model.VMs;
import com.redhat.rhevm.api.powershell.model.PowerShellSystemStats;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPool;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.resource.ApiResource;

public class PowerShellApiResource
    extends AbstractPowerShellResource
    implements ApiResource {

    private static final String SEARCH_RELATION = "/search";
    private static final String SEARCH_TEMPLATE = "?search={query}";

    protected final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    private static API api = new API();

    static {
        addLink("capabilities", false);
        addLink("clusters");
        addLink("datacenters");
        addLink("hosts");
        addLink("networks", false); // powershell has no select-network command
        addLink("roles", false);
        addLink("storagedomains");
        addLink("tags", false);
        addLink("templates");
        addLink("users");
        addLink("vmpools");
        addLink("vms");
    }

    private static void addLink(String rel, boolean searchable) {
        Link link = new Link();
        link.setRel(rel);
        link.setHref(rel + "/");
        api.getLinks().add(link);

        if (searchable) {
            link = new Link();
            link.setRel(rel + SEARCH_RELATION);
            link.setHref(rel + "/" + SEARCH_TEMPLATE);
            api.getLinks().add(link);
        }
    }

    public PowerShellApiResource() {
    }

    public PowerShellApiResource(Executor executor,
                                 PowerShellPoolMap shellPools,
                                 PowerShellParser parser) {
        super(executor, shellPools, parser);
    }

    private static void addLink(String rel) {
        addLink(rel, true);
    }

    private String addPath(UriBuilder uriBuilder, Link link) {
        String query = "";
        String path = link.getHref();

        // otherwise UriBuilder.build() will substitute {query}
        if (path.contains("?")) {
            query = path.substring(path.indexOf("?"));
            path = path.substring(0, path.indexOf("?") - 1);
        }

        link = JAXBHelper.clone(OBJECT_FACTORY.createLink(link));
        link.setHref(uriBuilder.clone().path(path).build() + query);

        return LinkHeader.format(link);
    }

    private void addHeader(Response.ResponseBuilder responseBuilder, UriBuilder uriBuilder) {
        // concantenate links in a single header with a comma-separated value,
        // which is the canonical form according to:
        // http://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2
        //
        StringBuffer header = new StringBuffer();

        for (Link l : api.getLinks()) {
            header.append(addPath(uriBuilder, l)).append(",");
        }

        header.setLength(header.length() - 1);

        responseBuilder.header("Link", header);
    }

    private Response.ResponseBuilder getResponseBuilder(UriInfo uriInfo) {
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();

        Response.ResponseBuilder responseBuilder = Response.ok();

        addHeader(responseBuilder, uriBuilder);

        return responseBuilder;
    }

    @Override
    public Response head(UriInfo uriInfo) {
        return getResponseBuilder(uriInfo).build();
    }

    @Override
    public Response get(UriInfo uriInfo) {
        API ret = JAXBHelper.clone(OBJECT_FACTORY.createApi(api));
        ret = addSummary(ret);
        return getResponseBuilder(uriInfo).entity(ret).build();
    }

    private PowerShellSystemStats runAndParse(String command) {
        return PowerShellSystemStats.parse(getParser(), PowerShellCmd.runCommand(getPool(), command));
    }

    private API addSummary(API api) {
        PowerShellSystemStats stats = runAndParse("get-systemstatistics");

        ApiSummary summary = new ApiSummary();

        summary.setVMs(new VMs());
        summary.getVMs().setTotal(stats.totalVms);
        summary.getVMs().setActive(stats.activeVms);

        summary.setHosts(new Hosts());
        summary.getHosts().setTotal(stats.totalHosts);
        summary.getHosts().setActive(stats.activeHosts);

        summary.setUsers(new Users());
        summary.getUsers().setTotal(stats.totalUsers);
        summary.getUsers().setActive(stats.activeUsers);

        summary.setStorageDomains(new StorageDomains());
        summary.getStorageDomains().setTotal(stats.totalStorageDomains);
        summary.getStorageDomains().setActive(stats.activeStorageDomains);

        api.setSummary(summary);

        return api;
    }
}

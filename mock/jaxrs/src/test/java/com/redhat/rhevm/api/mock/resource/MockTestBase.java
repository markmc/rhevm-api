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
package com.redhat.rhevm.api.mock.resource;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MultivaluedMap;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.API;
import com.redhat.rhevm.api.model.Capabilities;
import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.Clusters;
import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.model.Hosts;
import com.redhat.rhevm.api.model.LinkHeader;
import com.redhat.rhevm.api.model.Link;
import com.redhat.rhevm.api.model.Network;
import com.redhat.rhevm.api.model.Networks;
import com.redhat.rhevm.api.model.Role;
import com.redhat.rhevm.api.model.Roles;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.StorageDomains;
import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.model.Templates;
import com.redhat.rhevm.api.model.User;
import com.redhat.rhevm.api.model.Users;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.model.VMs;
import com.redhat.rhevm.api.model.VmPool;
import com.redhat.rhevm.api.model.VmPools;
import com.redhat.rhevm.api.resource.MediaType;
import com.redhat.rhevm.api.common.resource.DefaultCapabilitiesResource;

public class MockTestBase extends Assert {
    private final String host = "localhost";
    private final int port = 8989;
    protected final String API_URI = String.format("http://%s:%d/", host, port);

    private TJWSEmbeddedJaxrsServer server = new TJWSEmbeddedJaxrsServer();

    /* FIXME:
     * unify the client and server interfaces
     *
     * one interesting thing, though, is that if the client uses
     * an @Path("/vms") annotated interface, it is hardcoding
     * knowledge of the URI structure ... and we don't want that
     */

    @Path("/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_X_YAML})
    protected interface ApiResource {
        @HEAD public ClientResponse<Object> head();
        @GET public ClientResponse<API> get();
    }
    protected static ApiResource api;

    @Path("/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_X_YAML})
    protected interface VmsResource {
        @GET public VMs list(@QueryParam("search") String query);
        @GET @Path("{id}") public VM get(@PathParam("id") String id);
        @PUT @Path("{id}") @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_X_YAML}) public VM update(@PathParam("id") String id, VM vm);
    }

    protected VmsResource createVmsResource(String uri) {
        return ProxyFactory.create(VmsResource.class, uri);
    }

    @Path("/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_X_YAML})
    protected interface VmPoolsResource {
        @GET public VmPools list(@QueryParam("search") String query);
        @GET @Path("{id}") public VmPool get(@PathParam("id") String id);
        @POST @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_X_YAML}) public VmPool add(VmPool pool);
        @DELETE @Path("{id}") public void remove(@PathParam("id") String id);
    }

    protected VmPoolsResource createVmPoolsResource(String uri) {
        return ProxyFactory.create(VmPoolsResource.class, uri);
    }

    @Path("/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_X_YAML})
    protected interface HostsResource {
        @GET public Hosts list(@QueryParam("search") String query);
        @GET @Path("{id}") public Host get(@PathParam("id") String id);
        @PUT @Path("{id}") @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_X_YAML}) public Host update(@PathParam("id") String id, Host host);
    }

    protected HostsResource createHostsResource(String uri) {
        return ProxyFactory.create(HostsResource.class, uri);
    }

    @Path("/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_X_YAML})
    protected interface UsersResource {
        @GET public Users list(@QueryParam("search") String query);
        @GET @Path("{id}") public User get(@PathParam("id") String id);
        @POST @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_X_YAML}) public User add(User user);
        @DELETE @Path("{id}") public void remove(@PathParam("id") String id);
    }

    protected UsersResource createUsersResource(String uri) {
        return ProxyFactory.create(UsersResource.class, uri);
    }

    @Path("/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_X_YAML})
    protected interface RolesResource {
        @GET public Roles list(@QueryParam("search") String query);
        @GET @Path("{id}") public Role get(@PathParam("id") String id);
        @POST @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_X_YAML}) public Role add(Role role);
        @DELETE @Path("{id}") public void remove(@PathParam("id") String id);
    }

    protected RolesResource createRolesResource(String uri) {
        return ProxyFactory.create(RolesResource.class, uri);
    }

    @Path("/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_X_YAML})
    protected interface NetworksResource {
        @GET public Networks list();
        @GET @Path("{id}") public Network get(@PathParam("id") String id);
        @PUT @Path("{id}") @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_X_YAML}) public Network update(@PathParam("id") String id, Network network);
    }

    protected NetworksResource createNetworksResource(String uri) {
        return ProxyFactory.create(NetworksResource.class, uri);
    }

    @Path("/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_X_YAML})
    protected interface TemplatesResource {
        @GET public Templates list();
        @GET @Path("{id}") public Template get(@PathParam("id") String id);
    }

    protected TemplatesResource createTemplatesResource(String uri) {
        return ProxyFactory.create(TemplatesResource.class, uri);
    }

    @Path("/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_X_YAML})
    protected interface ClustersResource {
        @GET public Clusters list(@QueryParam("search") String query);
        @GET @Path("{id}") public Cluster get(@PathParam("id") String id);
        @PUT @Path("{id}") @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_X_YAML}) public Cluster update(@PathParam("id") String id, Cluster cluster);
    }

    protected ClustersResource createClustersResource(String uri) {
        return ProxyFactory.create(ClustersResource.class, uri);
    }

    @Path("/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_X_YAML})
    protected interface CapabilitiesResource {
        @GET public Capabilities get();
    }

    protected CapabilitiesResource createCapabilitiesResource(String uri) {
        return ProxyFactory.create(CapabilitiesResource.class, uri);
    }

    @Path("/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_X_YAML})
    protected interface StorageDomainsResource {
        @GET public StorageDomains list(@QueryParam("search") String query);
        @GET @Path("{id}") public StorageDomain get(@PathParam("id") String id);
    }

    protected StorageDomainsResource createStorageDomainsResource(String uri) {
        return ProxyFactory.create(StorageDomainsResource.class, uri);
    }

    @Path("/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_X_YAML})
    protected interface ActionResource {
        @POST @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_X_YAML}) public void post(Action action);
    }

    protected ActionResource createActionResource(String uri) {
        return ProxyFactory.create(ActionResource.class, uri);
    }

    protected Link getEntryPoint(String rel) {
        MultivaluedMap<String, String> headers = api.head().getHeaders();
        for (String s : headers.get("Link")) {
            for (String t : s.split(",")) {
                Link link = LinkHeader.parse(t);
                if (rel.equals(link.getRel())) {
                    return link;
                }
            }
        }
        throw new RuntimeException("No '" + rel + "' Link header found");
    }

    @Before
    public void setup() throws Exception {
        Executor executor =
            new ThreadPoolExecutor(5, 100, 3600, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        server.setPort(port);
        server.start();

        server.getDeployment().getDispatcher().getRegistry().addSingletonResource(new MockApiResource());

        MockVmsResource vms = new MockVmsResource();
        vms.setExecutor(executor);
        vms.populate();
        server.getDeployment().getDispatcher().getRegistry().addSingletonResource(vms);

        MockVmPoolsResource pools = new MockVmPoolsResource();
        pools.setExecutor(executor);
        pools.populate();
        server.getDeployment().getDispatcher().getRegistry().addSingletonResource(pools);

        MockHostsResource hosts = new MockHostsResource();
        hosts.setExecutor(executor);
        hosts.populate();
        server.getDeployment().getDispatcher().getRegistry().addSingletonResource(hosts);

        MockUsersResource users = new MockUsersResource();
        users.setExecutor(executor);
        users.populate();
        server.getDeployment().getDispatcher().getRegistry().addSingletonResource(users);

        MockRolesResource roles = new MockRolesResource();
        roles.setExecutor(executor);
        roles.populate();
        server.getDeployment().getDispatcher().getRegistry().addSingletonResource(roles);

        MockNetworksResource networks = new MockNetworksResource();
        networks.setExecutor(executor);
        networks.populate();
        server.getDeployment().getDispatcher().getRegistry().addSingletonResource(networks);

        MockTemplatesResource templates = new MockTemplatesResource();
        templates.setExecutor(executor);
        templates.populate();
        server.getDeployment().getDispatcher().getRegistry().addSingletonResource(templates);

        MockClustersResource clusters = new MockClustersResource();
        clusters.setExecutor(executor);
        clusters.populate();
        server.getDeployment().getDispatcher().getRegistry().addSingletonResource(clusters);

        DefaultCapabilitiesResource caps = new DefaultCapabilitiesResource();
        server.getDeployment().getDispatcher().getRegistry().addSingletonResource(caps);

        MockStorageDomainsResource storageDomains = new MockStorageDomainsResource();
        storageDomains.setExecutor(executor);
        storageDomains.populate();
        server.getDeployment().getDispatcher().getRegistry().addSingletonResource(storageDomains);

        RegisterBuiltin.register(ResteasyProviderFactory.getInstance());

        api = ProxyFactory.create(ApiResource.class, API_URI);
    }

    @After
    public void destroy() throws Exception {
        server.stop();
    }
}

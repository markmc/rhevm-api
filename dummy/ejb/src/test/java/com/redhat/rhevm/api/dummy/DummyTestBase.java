package com.redhat.rhevm.api.dummy;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MultivaluedMap;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import com.redhat.rhevm.api.Link;
import com.redhat.rhevm.api.MediaType;
import com.redhat.rhevm.api.VM;

public class DummyTestBase extends Assert
{
	private final String host = "localhost";
	private final int port = 8989;
	private final String uri = String.format("http://%s:%d/", host, port);

	private TJWSEmbeddedJaxrsServer server = new TJWSEmbeddedJaxrsServer();

	/* FIXME:
	 * unify the client and server interfaces
	 *
	 * one interesting thing, though, is that if the client uses
	 * an @Path("/vms") annotated interface, it is hardcoding
	 * knowledge of the URI structure ... and we don't want that
	 */

	@Path("/")
	protected interface API {
		@HEAD public ClientResponse head();
	}
	protected static API api;

	@Path("/")
	@Produces(MediaType.APPLICATION_XML)
	protected interface VMs {
		@GET public List<VM> list();
		@GET @Path("{id}") public VM get(@PathParam("id") String id);
	}
	protected VMs createVMs(String uri) {
		return ProxyFactory.create(VMs.class, uri);
	}

	protected Link getEntryPoint(String rel) {
		MultivaluedMap<String, String> headers = api.head().getHeaders();
		for (String s : headers.get("Link")) {
			/* FIXME: WTF are the headers concatenated into a single string? */
			for (String t : s.split(",")) {
				Link link = Link.valueOf(t);
				if (rel.equals(link.getRel()))
					return link;
			}
		}
		throw new RuntimeException("No '" + rel + "' Link header found");
	}

	@Before
	public void setup() throws Exception {
		server.setPort(port);
		server.start();
		server.getDeployment().getDispatcher().getRegistry().addPerRequestResource(DummyAPI.class);
		server.getDeployment().getDispatcher().getRegistry().addPerRequestResource(DummyVMs.class);

		RegisterBuiltin.register(ResteasyProviderFactory.getInstance());

		api = ProxyFactory.create(API.class, uri.toString());
	}

	@After
	public void destroy() throws Exception {
		server.stop();
	}
}

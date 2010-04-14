package com.redhat.rhevm.api.dummy;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;

public class DummyVMsTest
{
	private TJWSEmbeddedJaxrsServer server = new TJWSEmbeddedJaxrsServer();

	@Before
	public void setup() throws Exception {
		server.setPort(8989);
		server.start();
		server.getDeployment().getDispatcher().getRegistry().addPerRequestResource(DummyVMs.class);
	}

	@After
	public void destroy() throws Exception {
		if (server != null) {
			server.stop();
		}
	}

	@Test
	public void testGet() throws Exception {
		URL url = new URL("http://localhost:8989/vms");
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.setRequestMethod("GET");

		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

		String line = reader.readLine();
		while (line != null) {
			line = reader.readLine();
		}

		Assert.assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());

		connection.disconnect();
	}
}

package com.redhat.rhevm.api.dummy;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.jboss.resteasy.client.ClientResponse;
import com.redhat.rhevm.api.Link;

public class DummyAPITest extends DummyTestBase
{
	@Test
	public void testEntryPoint() throws Exception {
		ClientResponse response = api.head();

		assertEquals(Response.Status.Family.SUCCESSFUL, response.getResponseStatus().getFamily());
		assertEquals(Response.Status.OK, response.getResponseStatus());

		MultivaluedMap<String, String> headers = response.getHeaders();

		List<String> linkHeaders = headers.get("Link");

		assertNotNull(linkHeaders);

		List<Link> links = new ArrayList<Link>();

		for (String s : linkHeaders) {
			/* FIXME: WTF are the headers concatenated into a single string? */
			for (String t : s.split(",")) {
				Link l = Link.valueOf(t);
				assertEquals(t, l.toString());
				links.add(l);
			}
		}

		assertEquals(2, links.size());
		assertEquals("hosts", links.get(0).getRel());
		assertEquals("vms", links.get(1).getRel());
	}
}

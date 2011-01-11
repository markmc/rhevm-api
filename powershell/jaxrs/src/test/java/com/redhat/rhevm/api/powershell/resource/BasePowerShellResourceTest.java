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

import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Arrays;

import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Assert;
import org.junit.Ignore;

import com.redhat.rhevm.api.model.BaseResource;
import com.redhat.rhevm.api.model.Fault;
import com.redhat.rhevm.api.model.Link;
import com.redhat.rhevm.api.powershell.util.PowerShellTestUtils;

@Ignore
public class BasePowerShellResourceTest extends Assert {

    protected static final String URI_ROOT = "http://localhost:8099";
    protected static final String BASE_PATH = "/rhevm-api-powershell";
    protected static final String URI_BASE = URI_ROOT + BASE_PATH;
    protected static final String SLASH = "/";
    protected static final String QUERY = "name=*r*s";

    protected String formatXmlReturn(String type, String[] names, String[] descriptions) {
        return formatXmlReturn(type, names, descriptions, new String[]{});
    }

    protected String formatXmlReturn(String type, String[] names, String[] descriptions, String[] args) {
        String[][] perNameArgs = new String[names.length][];
        for (int i = 0; i < names.length; i++) {
            perNameArgs[i] = args;
        }
        return formatXmlReturn(type, names, descriptions, perNameArgs);
    }

    protected String formatXmlReturn(String type, String[] names, String[] descriptions, String[][] args) {
        String tmpl = PowerShellTestUtils.readClassPathFile(type + ".tmpl");
        StringBuilder buffer = new StringBuilder();
        buffer.append("<?xml version=\"1.0\"?>");
        buffer.append("<Objects>");
        for (int i = 0; i < names.length; i++) {
            buffer.append(MessageFormat.format(tmpl, buildArgs(Integer.toString(names[i].hashCode()),
                                                               names[i],
                                                               descriptions[i],
                                                               args[i])));
        }
        buffer.append("</Objects>");
        return buffer.toString();
    }

    protected String formatVersion(int major, int minor) {
        String tmpl = PowerShellTestUtils.readClassPathFile("version.tmpl");
        StringBuilder buffer = new StringBuilder();
        buffer.append("<?xml version=\"1.0\"?>");
        buffer.append("<Objects>");
        buffer.append(MessageFormat.format(tmpl, new Object[] { Integer.toString(major), Integer.toString(minor) }));
        buffer.append("</Objects>");
        return buffer.toString();
    }

    protected void verifyIncompleteException(WebApplicationException wae, String type, String method, String... fields) {
        assertEquals(400, wae.getResponse().getStatus());
        Fault fault = (Fault)wae.getResponse().getEntity();
        assertNotNull(fault);
        assertEquals("Incomplete parameters", fault.getReason());
        assertEquals(type + " " +  Arrays.asList(fields) + " required for " + method, fault.getDetail());
    }

    static Object[] buildArgs(String id, String name, String description, Object[] args) {
        Object[] newArgs = new Object[args.length + 3];
        newArgs[0] = id;
        newArgs[1] = name;
        newArgs[2] = description;
        System.arraycopy(args, 0, newArgs, 3, args.length);
        return newArgs;
    }

    protected QueryParam getQueryParam() {
        return new QueryParam() {
            public String value() {
                return QUERY;
            }
            @Override
            public Class<? extends Annotation> annotationType() {
                return null;
            }
        };
    }

    protected UriInfo setUpBasicUriExpectations() {
        UriInfo uriInfo = createMock(UriInfo.class);
        expect(uriInfo.getBaseUri()).andReturn(URI.create(URI_BASE + SLASH)).anyTimes();
        return uriInfo;
    }

    protected void verifyResponse(Response r, String name, String description, String collectionName) {
        assertEquals("unexpected status", 201, r.getStatus());
        Object entity = r.getEntity();
        assertTrue("expect response entity", entity instanceof BaseResource);
        BaseResource model = (BaseResource)entity;
        assertEquals(Integer.toString(name.hashCode()), model.getId());
        assertEquals(name, model.getName());
        assertEquals(description, model.getDescription());
        assertNotNull(r.getMetadata().get("Location"));
        assertTrue("expected location header",
                   r.getMetadata().get("Location").size() > 0);
        assertEquals("unexpected location header",
                     URI_ROOT + SLASH + collectionName + SLASH + name.hashCode(),
                     r.getMetadata().get("Location").get(0).toString());
    }

    protected static void verifyLinks(BaseResource model) {
        assertNotNull(model.getHref());
        assertTrue(model.getHref().startsWith("/rhevm-api-powershell"));
        for (Link link : model.getLinks()) {
            assertTrue(link.getHref().startsWith("/rhevm-api-powershell"));
        }
    }

    protected static void verifyLink(BaseResource model, String rel) {
        Link link = null;
        for (Link l : model.getLinks()) {
            if (rel.equals(l.getRel())) {
                link = l;
                break;
            }
        }
        assertNotNull(link);
        assertTrue(link.getHref().startsWith("/rhevm-api-powershell"));
    }

    protected static String[] asArray(String s) {
        return new String[] { s };
    }

    protected static String[] asArrayV(String... s) {
        return s ;
    }

    protected static String[][] asArray(String[] a) {
        return new String[][] { a };
    }

    protected static String asId(String name) {
        return Integer.toString(name.hashCode());
    }
}

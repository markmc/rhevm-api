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
package com.redhat.rhevm.api.common.security.auth;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import com.redhat.rhevm.api.common.security.auth.BasicAuthorizationScheme;
import com.redhat.rhevm.api.common.security.auth.Principal;
import com.redhat.rhevm.api.common.security.auth.Scheme;

import junit.framework.Assert;

import static org.easymock.classextension.EasyMock.expect;

public class BasicAuthorizationSchemeTest extends Assert {

    private static final String SHORT_CREDENTIALS = "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==";
    private static final String UPN_LONG_CREDENTIALS = "Basic QWxhZGRpbkBNYWdocmViOm9wZW4gc2VzYW1l";
    private static final String LEGACY_LONG_CREDENTIALS = "Basic TWFnaHJlYlxBbGFkZGluOm9wZW4gc2VzYW1l";
    private static final String BAD_CREDENTIALS = "Basic 123456";
    private static final String DIGEST_CREDENTIALS =
        "Digest username=\"Mufasa\",realm=\"testrealm@host.com\","
        + "nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\",uri=\"/dir/index.html\","
        + "qop=auth,nc=00000001,cnonce=\"0a4f113b\","
        + "response=\"6629fae49393a05397450978507c4ef1\","
        + "opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"";
    private static final String USER = "Aladdin";
    private static final String SECRET = "open sesame";
    private static final String DOMAIN = "Maghreb";


    private Scheme scheme;
    private IMocksControl control;

    @Before
    public void setUp() {
        control = EasyMock.createNiceControl();
        scheme = new BasicAuthorizationScheme();
    }

    @Test
    public void testSchemeName() {
        assertEquals("Basic", scheme.getName());
    }

    @Test
    public void testDecodeShortCredentials() {
        Principal principal = scheme.decode(setUpHeadersExpectation(SHORT_CREDENTIALS));
        assertNotNull(principal);
        assertEquals(USER, principal.getUser());
        assertEquals(SECRET, principal.getSecret());
        assertNull(principal.getDomain());
        control.verify();
    }

    @Test
    public void testDecodeUpnLongCredentials() {
        Principal principal = scheme.decode(setUpHeadersExpectation(UPN_LONG_CREDENTIALS));
        assertNotNull(principal);
        assertEquals(USER, principal.getUser());
        assertEquals(SECRET, principal.getSecret());
        assertEquals(DOMAIN, principal.getDomain());
        control.verify();
    }

    @Test
    public void testDecodeLegacyLongCredentials() {
        Principal principal = scheme.decode(setUpHeadersExpectation(LEGACY_LONG_CREDENTIALS));
        assertNotNull(principal);
        assertEquals(USER, principal.getUser());
        assertEquals(SECRET, principal.getSecret());
        assertEquals(DOMAIN, principal.getDomain());
        control.verify();
    }

    @Test
    public void testDecodeBadCredentials() {
        Principal principal = scheme.decode(setUpHeadersExpectation(BAD_CREDENTIALS));
        assertNull(principal);
        control.verify();
    }

    @Test
    public void testDecodeDigestCredentials() {
        Principal principal = scheme.decode(setUpHeadersExpectation(DIGEST_CREDENTIALS));
        assertNull(principal);
        control.verify();
    }

    private HttpHeaders setUpHeadersExpectation(String credentials) {
        HttpHeaders headers = control.createMock(HttpHeaders.class);
        List<String> authHeaders = new ArrayList<String>();
        authHeaders.add(credentials);
        expect(headers.getRequestHeader(HttpHeaders.AUTHORIZATION)).andReturn(authHeaders);
        control.replay();
        return headers;
    }

}

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
package com.redhat.rhevm.api.common.security;

import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.Status;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.HttpRequest;
import org.junit.Before;
import org.junit.Test;

import com.redhat.rhevm.api.common.invocation.Current;

import junit.framework.Assert;

public class AuthorizationChallengerTest extends Assert {

    private static final String CREDENTIALS = "Basic TWFnaHJlYlxBbGFkZGluOm9wZW4gc2VzYW1l";
    private static final String USER = "Aladdin";
    private static final String SECRET = "open sesame";
    private static final String DOMAIN = "Maghreb";

    private AuthorizationChallenger challenger;
    private IMocksControl control;

    @Before
    public void setUp() {
        control = EasyMock.createNiceControl();
    }

    @Test
    public void testAuthHeaderPresent() {
        challenger = new AuthorizationChallenger();
        ResourceMethod resource = control.createMock(ResourceMethod.class);
        ServerResponse response = challenger.preProcess(setUpRequestExpectations(CREDENTIALS), resource);
        assertNull(response);
        control.verify();
    }

    @Test
    public void testAuthHeaderMissing() {
        challenger = new AuthorizationChallenger();
        ResourceMethod resource = control.createMock(ResourceMethod.class);
        ServerResponse response = challenger.preProcess(setUpRequestExpectations(null), resource);
        assertNotNull(response);
        assertEquals(Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        control.verify();
    }

    @Test
    public void testAuthHeaderValidateTrue() {
        challenger = new ValidatingChallenger(true);
        ResourceMethod resource = control.createMock(ResourceMethod.class);
        ServerResponse response = challenger.preProcess(setUpRequestExpectations(CREDENTIALS, true), resource);
        assertNull(response);
        control.verify();
    }

    @Test
    public void testAuthHeaderValidateFalse() {
        challenger = new ValidatingChallenger(false);
        ResourceMethod resource = control.createMock(ResourceMethod.class);
        ServerResponse response = challenger.preProcess(setUpRequestExpectations(CREDENTIALS, false), resource);
        assertNotNull(response);
        assertEquals(Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        control.verify();
    }

    private HttpRequest setUpRequestExpectations(String credentials) {
        return setUpRequestExpectations(credentials, credentials != null);
    }

    private HttpRequest setUpRequestExpectations(String credentials, boolean valid) {
        Authorizer authorizer = control.createMock(Authorizer.class);
        challenger.setAuthorizer(authorizer);
        Current current = control.createMock(Current.class);
        challenger.setCurrent(current);
        HttpRequest request = control.createMock(HttpRequest.class);
        HttpHeaders headers = control.createMock(HttpHeaders.class);
        expect(request.getHttpHeaders()).andReturn(headers);
        List<String> authHeaders = new ArrayList<String>();
        expect(headers.getRequestHeader(HttpHeaders.AUTHORIZATION)).andReturn(authHeaders);
        if (credentials != null) {
            Principal principal = new Principal(USER, SECRET, DOMAIN);
            expect(authorizer.decode(headers)).andReturn(principal);
            authHeaders.add(credentials);
            if (valid) {
                current.set(principal);
                EasyMock.expectLastCall();
            }
        }
        control.replay();
        return request;
    }

    protected class ValidatingChallenger extends AuthorizationChallenger {
        private boolean valid;

        protected ValidatingChallenger(boolean valid) {
            this.valid = valid;
        }

        @Override
        protected boolean validate(Principal principal) {
            return valid;
        }
    }
}

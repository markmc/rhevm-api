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

import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.annotations.interception.Precedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;

import com.redhat.rhevm.api.common.invocation.Current;

@Provider
@ServerInterceptor
@Precedence("SECURITY")
public class AuthorizationChallenger implements PreProcessInterceptor {

    private String realm;
    private Authorizer authorizer;
    private Current current;

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public void setAuthorizer(Authorizer authorizer) {
        this.authorizer = authorizer;
    }

    public void setCurrent(Current current) {
        this.current = current;
    }

    @Override
    public ServerResponse preProcess(HttpRequest request, ResourceMethod method) throws Failure, WebApplicationException {
        ServerResponse response = null;
        HttpHeaders headers = request.getHttpHeaders();
        List<String> auth = headers.getRequestHeader(HttpHeaders.AUTHORIZATION);
        if (auth == null || auth.size() == 0) {
            response = ServerResponse.copyIfNotServerResponse(
                           Response.status(Status.UNAUTHORIZED)
                                   .header(HttpHeaders.WWW_AUTHENTICATE, authorizer.getChallenge(realm))
                                   .build());
        } else {
            Principal principal = authorizer.decode(headers);
            current.set(principal);
        }
        return response;
    }
}

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

import java.text.MessageFormat;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.codec.binary.Base64;


public class BasicAuthorizationScheme implements Scheme {

    private static final String SCHEME = "Basic";
    private static final String CHALLANGE_TEMPLATE = SCHEME + " realm = \"{0}\"";

    private static String USER_PASS_SEPARATOR = ":";
    private static String USER_DOMAIN_SEPARATOR = "\\";

    @Override
    public String getName() {
        return SCHEME;
    }

    @Override
    public String getChallenge(String realm) {
        return MessageFormat.format(CHALLANGE_TEMPLATE, realm);
    }

    @Override
    public Principal decode(HttpHeaders headers) {
        Principal principal = null;
        if (headers != null) {
            List<String> auth = headers.getRequestHeader(HttpHeaders.AUTHORIZATION);
            if (auth != null && auth.size() > 0) {
                String credentials = auth.get(0);
                if (credentials.trim().startsWith(SCHEME)) {
                    principal = decode(credentials);
                }
            }
        }
        return principal;
    }

    private Principal decode(String credentials) {
        Principal principal = null;
        try {
            credentials = credentials.trim().substring(SCHEME.length()).trim();
            String userPass = new String(Base64.decodeBase64(credentials));
            String[] creds = userPass.split(USER_PASS_SEPARATOR, 2);
            if (creds != null && creds.length == 2) {
                int index = creds[0].indexOf(USER_DOMAIN_SEPARATOR);
                if (index != -1) {
                    String domain = creds[0].substring(0, index);
                    String user = creds[0].substring(index + 1);
                    principal = new Principal(user, creds[1], domain);
                } else {
                    principal = new Principal(creds[0], creds[1]);
                }
            }
        } catch (Exception e) {
            // let principal remain null
        }
        return principal;
    }
}

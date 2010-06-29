package com.redhat.rhevm.api.common.security;

/**
 * Encasulates user identity.
 */
public class Principal {
    private String user;
    private String secret;
    private String domain;

    public Principal(String user, String secret) {
        this(user, secret, null);
    }

    public Principal(String user, String secret, String domain) {
        this.user = user;
        this.secret = secret;
        this.domain = domain;
    }

    public String getUser() {
        return user;
    }

    public String getSecret() {
        return secret;
    }

    public String getDomain() {
        return domain;
    }
}

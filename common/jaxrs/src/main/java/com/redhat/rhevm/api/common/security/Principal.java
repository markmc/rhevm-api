package com.redhat.rhevm.api.common.security;

/**
 * Encasulates user identity.
 */
public class Principal {

    public static final Principal NONE = new Principal(null, null, null);

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

    @Override public boolean equals(Object o) {
        if (!(o instanceof Principal))
            return false;
        Principal auth = (Principal)o;
        if ((domain == null) ? auth.domain != null : !domain.equals(auth.domain))
            return false;
        if ((user == null) ? auth.user != null : !user.equals(auth.user))
            return false;
        if ((secret == null) ? auth.secret != null : !secret.equals(auth.secret))
            return false;
        return true;
    }

    @Override public int hashCode() {
        int result = 17;
        if (domain != null)
            result = 31 * result + domain.hashCode();
        if (user != null)
            result = 31 * result + user.hashCode();
        if (secret != null)
            result = 31 * result + secret.hashCode();
        return result;
    }
}

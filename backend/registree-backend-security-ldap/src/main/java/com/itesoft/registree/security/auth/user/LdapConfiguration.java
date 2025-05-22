package com.itesoft.registree.security.auth.user;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapClient;
import org.springframework.ldap.core.LdapTemplate;

@Configuration
@ConfigurationProperties("ldap")
public class LdapConfiguration {
  public static class LdapAuthentication {
    private boolean useLdap;
    private String baseDn;
    private String userFilter;

    public boolean isUseLdap() {
      return useLdap;
    }

    public void setUseLdap(final boolean useLdap) {
      this.useLdap = useLdap;
    }

    public String getBaseDn() {
      return baseDn;
    }

    public void setBaseDn(final String baseDn) {
      this.baseDn = baseDn;
    }

    public String getUserFilter() {
      return userFilter;
    }

    public void setUserFilter(final String userFilter) {
      this.userFilter = userFilter;
    }
  }

  public static class LdapUserAttributes {
    private String username;
    private String firstName;
    private String lastName;

    public String getUsername() {
      return username;
    }

    public void setUsername(final String username) {
      this.username = username;
    }

    public String getFirstName() {
      return firstName;
    }

    public void setFirstName(final String firstName) {
      this.firstName = firstName;
    }

    public String getLastName() {
      return lastName;
    }

    public void setLastName(final String lastName) {
      this.lastName = lastName;
    }
  }

  public static class LdapUser {
    private LdapUserAttributes attributes;

    public LdapUserAttributes getAttributes() {
      return attributes;
    }

    public void setAttributes(final LdapUserAttributes attributes) {
      this.attributes = attributes;
    }
  }

  private LdapAuthentication auth;
  private LdapUser user;

  @Bean
  public LdapClient ldapClient(final LdapTemplate ldapTemplate) {
    return LdapClient.builder().contextSource(ldapTemplate.getContextSource()).build();
  }

  public LdapAuthentication getAuth() {
    return auth;
  }

  public void setAuth(final LdapAuthentication auth) {
    this.auth = auth;
  }

  public LdapUser getUser() {
    return user;
  }

  public void setUser(final LdapUser user) {
    this.user = user;
  }
}

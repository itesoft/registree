package com.itesoft.registree.security.auth.user;

import static org.springframework.ldap.query.LdapQueryBuilder.query;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notNull;

import java.util.List;
import java.util.Optional;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;

import jakarta.annotation.PostConstruct;

import com.itesoft.registree.dao.jpa.UserEntity;
import com.itesoft.registree.dao.jpa.UserRepository;
import com.itesoft.registree.security.auth.user.LdapConfiguration.LdapAuthentication;
import com.itesoft.registree.security.auth.user.LdapConfiguration.LdapUser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.LdapDataEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapClient;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.HardcodedFilter;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Component;

@Component
public class LdapUserAuthenticationProvider implements UserAuthenticationProvider {
  private static final Logger LOGGER = LoggerFactory.getLogger(LdapUserAuthenticationProvider.class);

  @Autowired
  private LdapTemplate ldapTemplate;

  @Autowired
  private LdapClient ldapClient;

  @Autowired
  private LdapConfiguration ldapConfiguration;

  @Autowired
  private UserRepository userRepository;

  @PostConstruct
  public void validateConfig() {
    final LdapAuthentication ldapAuthentication = ldapConfiguration.getAuth();
    if (ldapAuthentication == null || !ldapAuthentication.isUseLdap()) {
      return;
    }

    final LdapUser ldapUser = ldapConfiguration.getUser();
    notNull(ldapUser, "attributes must not be null when using ldap");
    notNull(ldapUser.getAttributes(), "attributes must not be null when using ldap");
    hasText(ldapUser.getAttributes().getUsername(),
            "username attribute must be defined when using ldap");
  }

  @Override
  public Long authenticate(final String username, final String password) {
    final LdapAuthentication ldapAuthentication = ldapConfiguration.getAuth();
    if (ldapAuthentication == null || !ldapAuthentication.isUseLdap()) {
      return null;
    }

    final AndFilter filter = new AndFilter()
      .and(new EqualsFilter(ldapConfiguration.getUser().getAttributes().getUsername(), username));
    final String userFilter = ldapConfiguration.getAuth().getUserFilter();
    if (userFilter != null) {
      filter.and(new HardcodedFilter(userFilter));
    }

    final LdapQueryBuilder queryBuilder = query();
    if (ldapConfiguration.getAuth().getBaseDn() != null) {
      queryBuilder.base(ldapConfiguration.getAuth().getBaseDn());
    }
    final LdapQuery query = queryBuilder.filter(filter);

    final List<LdapDataEntry> searchResult = ldapClient.search()
      .query(query)
      .toEntryList();

    if (searchResult.isEmpty()) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug(String.format("Ldap returned no result when searching user %s", userFilter));
      }
      return null;
    }

    if (searchResult.size() > 1) {
      LOGGER.warn(String.format("Ldap returned %d results when searching user %s", searchResult.size(), userFilter));
      return null;
    }
    final LdapDataEntry entry = searchResult.get(0);

    final LdapContextSource contextSource = (LdapContextSource) ldapTemplate.getContextSource();
    final String dn = LdapNameBuilder.newInstance(contextSource.getBaseLdapName())
      .add(entry.getDn())
      .build()
      .toString();
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(String.format("Found user %s, dn is %s", username, dn));
    }

    try {
      final DirContext context = contextSource.getContext(dn, password);
      context.close();
    } catch (final Exception exception) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Login failed", exception);
      }
      return null;
    }

    try {
      return createOrUpdateUser(entry);
    } catch (final NamingException exception) {
      LOGGER.error("User creation failed", exception);
      return null;
    }
  }

  private long createOrUpdateUser(final LdapDataEntry entry) throws NamingException {
    final String username = getAttribute(entry, ldapConfiguration.getUser().getAttributes().getUsername());
    final UserEntity userEntity;
    final Optional<UserEntity> optional = userRepository.findByUsername(username);
    if (optional.isPresent()) {
      userEntity = optional.get();
    } else {
      userEntity = new UserEntity();
    }
    final String firstName = getAttribute(entry, ldapConfiguration.getUser().getAttributes().getFirstName());
    final String lastName = getAttribute(entry, ldapConfiguration.getUser().getAttributes().getLastName());

    userEntity.setUsername(username);
    userEntity.setFirstName(firstName);
    userEntity.setLastName(lastName);

    final UserEntity result = userRepository.save(userEntity);
    return result.getId();
  }

  private String getAttribute(final LdapDataEntry entry,
                              final String name)
    throws NamingException {
    if (name == null) {
      return null;
    }
    final Attributes attributes = entry.getAttributes();
    final Attribute attribute = attributes.get(name);
    if (attribute == null) {
      return null;
    }
    return (String) attribute.get();
  }
}

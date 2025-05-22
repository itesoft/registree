package com.itesoft.registree;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.ldap.LdapRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;

@SpringBootApplication
@EnableAutoConfiguration(exclude = { ErrorMvcAutoConfiguration.class,
                                     SecurityAutoConfiguration.class,
                                     UserDetailsServiceAutoConfiguration.class,
                                     LdapRepositoriesAutoConfiguration.class })
public final class Application {
  public static void main(final String[] args) {
    SpringApplication.run(Application.class, args);
  }

  private Application() {
  }
}

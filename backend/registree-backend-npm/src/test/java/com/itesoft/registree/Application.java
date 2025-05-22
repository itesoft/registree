package com.itesoft.registree;

import com.itesoft.registree.spring.test.TypeMappedAnnotationArrayRuntimeHints;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.context.annotation.ImportRuntimeHints;

@SpringBootApplication
@EnableAutoConfiguration(exclude = { ErrorMvcAutoConfiguration.class,
                                     SecurityAutoConfiguration.class,
                                     UserDetailsServiceAutoConfiguration.class })
@ImportRuntimeHints(TypeMappedAnnotationArrayRuntimeHints.class)
public class Application {
  public static void main(final String[] args) {
    SpringApplication.run(Application.class, args);
  }
}

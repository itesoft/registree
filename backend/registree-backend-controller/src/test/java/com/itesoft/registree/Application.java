package com.itesoft.registree;

import com.itesoft.registree.spring.test.TypeMappedAnnotationArrayRuntimeHints;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;

@SpringBootApplication
@ImportRuntimeHints(TypeMappedAnnotationArrayRuntimeHints.class)
public class Application {
  public static void main(final String[] args) {
    SpringApplication.run(Application.class, args);
  }
}

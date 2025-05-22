package com.itesoft.registree.spring.test;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

public class TypeMappedAnnotationArrayRuntimeHints implements RuntimeHintsRegistrar {
  @Override
  public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
    hints.reflection().registerType(TypeReference.of("org.springframework.core.annotation.TypeMappedAnnotation[]"),
                                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
  }
}

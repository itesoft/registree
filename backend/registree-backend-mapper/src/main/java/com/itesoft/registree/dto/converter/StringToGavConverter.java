package com.itesoft.registree.dto.converter;

import com.itesoft.registree.dto.Gav;

import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToGavConverter implements Converter<String, Gav> {
  @Override
  public Gav convert(final String source) {
    if (source == null) {
      return null;
    }

    final String[] tab = source.split(":");
    if (tab.length == 1) {
      return Gav.builder()
        .name(source)
        .build();
    }

    if (tab.length == 2) {
      return Gav.builder()
        .name(tab[0])
        .version(tab[1])
        .build();
    }

    if (tab.length == 3) {
      return Gav.builder()
        .group(tab[0])
        .name(tab[1])
        .version(tab[2])
        .build();
    }

    throw new ConversionFailedException(TypeDescriptor.valueOf(String.class),
                                        TypeDescriptor.valueOf(Gav.class),
                                        source,
                                        new IllegalArgumentException("Given String does not have gav format"));
  }
}

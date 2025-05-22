package com.itesoft.registree.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Component
public class PicocliSpringFactory implements CommandLine.IFactory {
  private static final Logger LOGGER = LoggerFactory.getLogger(PicocliSpringFactory.class);

  private final CommandLine.IFactory fallbackFactory = CommandLine.defaultFactory();
  private final ApplicationContext applicationContext;

  public PicocliSpringFactory(final ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  @Override
  public <K> K create(final Class<K> clazz) throws Exception {
    try {
      return applicationContext.getAutowireCapableBeanFactory().createBean(clazz);
    } catch (final Exception exception) {
      LOGGER.warn(String.format("Unable to get bean of class %s from ApplicationContext, using fallback factory %s (%s)",
                                clazz,
                                fallbackFactory.getClass().getName(),
                                exception.toString()));
      return fallbackFactory.create(clazz);
    }
  }
}

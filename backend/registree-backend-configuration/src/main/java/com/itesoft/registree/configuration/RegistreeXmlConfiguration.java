package com.itesoft.registree.configuration;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;

@Configuration
public class RegistreeXmlConfiguration {
  private XmlMapper xmlMapper;

  @Autowired
  public void setXmlMapper(final MappingJackson2XmlHttpMessageConverter xmlConverter) {
    xmlMapper = (XmlMapper) xmlConverter.getObjectMapper();
  }

  public XmlMapper getXmlMapper() {
    return xmlMapper;
  }
}

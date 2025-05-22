package com.itesoft.registree.dao;

import static com.itesoft.registree.api.definition.WellKnownProperties.TOTAL_COUNT;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.github.perplexhub.rsql.RSQLJPASupport;
import io.github.perplexhub.rsql.jsonb.JsonbSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class SearchHelper {
  @Autowired
  private SearchConfiguration searchConfiguration;

  @Autowired
  private ConversionService conversionService;

  static {
    JsonbSupport.DATE_TIME_SUPPORT = true;
  }

  public <T> Specification<T> toSpecification(final String filter,
                                              final String sort,
                                              final Map<String, String> mapping) {
    if (filter != null && sort != null) {
      return RSQLJPASupport.<T>toSpecification(filter, mapping).and(RSQLJPASupport.toSort(sort, mapping));
    }
    if (filter != null) {
      return RSQLJPASupport.<T>toSpecification(filter, mapping);
    }
    if (sort != null) {
      return RSQLJPASupport.<T>toSort(sort, mapping);
    }
    return null;
  }

  public Pageable getPageable(final Integer page,
                              final Integer pageSize) {
    final int actualPage = page == null ? 0 : page;
    final int actualPageSize = pageSize == null ? searchConfiguration.defaultPageSize() : pageSize;
    return PageRequest.of(actualPage, actualPageSize);
  }

  public <T, ID, R> List<R> findAll(final Map<String, Object> extraProperties,
                                    final RegistreeRepository<T, ID> repository,
                                    final String filter,
                                    final String sort,
                                    final Integer page,
                                    final Integer pageSize,
                                    final Map<String, String> mapping,
                                    final Class<R> resultType) {
    final Specification<T> specification = toSpecification(filter, sort, mapping);
    final Pageable pageable = getPageable(page, pageSize);
    final Page<T> searchResult;

    if (specification == null) {
      searchResult = repository.findAll(pageable);
    } else {
      searchResult = repository.findAll(specification, pageable);
    }

    extraProperties.put(TOTAL_COUNT, Long.toString(searchResult.getTotalElements()));

    return searchResult.stream()
      .map(e -> conversionService.convert(e, resultType))
      .collect(Collectors.toList());
  }
}

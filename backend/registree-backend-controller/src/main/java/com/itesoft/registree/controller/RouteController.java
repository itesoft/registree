package com.itesoft.registree.controller;

import java.util.List;

import jakarta.transaction.Transactional;

import com.itesoft.registree.api.definition.RequestContext;
import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.dao.SearchHelper;
import com.itesoft.registree.dao.jpa.RouteRepository;
import com.itesoft.registree.dto.Route;
import com.itesoft.registree.dto.mapper.RouteEntityToRouteMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Transactional(rollbackOn = Throwable.class)
@Controller
public class RouteController {
  @Autowired
  private SearchHelper searchHelper;

  @Autowired
  private RouteRepository routeRepository;

  public List<Route> searchRoutes(final RequestContext requestContext,
                                  final ResponseContext responseContext,
                                  final String filter,
                                  final String sort,
                                  final Integer page,
                                  final Integer pageSize) {
    return searchHelper.findAll(responseContext.getExtraProperties(),
                                routeRepository,
                                filter,
                                sort,
                                page,
                                pageSize,
                                RouteEntityToRouteMapper.PROPERTY_MAPPINGS,
                                Route.class);
  }
}

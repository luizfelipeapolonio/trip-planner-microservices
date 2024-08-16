package com.felipe.trip_planner_gateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;

public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
  @Override
  public GatewayFilter apply(AuthenticationFilter.Config config) {
    return null;
  }

  public static class Config {}
}

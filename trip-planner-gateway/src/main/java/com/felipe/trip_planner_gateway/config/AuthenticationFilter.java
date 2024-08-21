package com.felipe.trip_planner_gateway.config;

import com.felipe.trip_planner_gateway.dtos.ValidatedUserDTO;
import com.felipe.trip_planner_gateway.exceptions.MissingAuthException;
import com.felipe.trip_planner_gateway.services.AuthService;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

  private final RouteValidator routeValidator;
  private final AuthService authService;

  public AuthenticationFilter(RouteValidator routeValidator, AuthService authService) {
    super(Config.class);
    this.routeValidator = routeValidator;
    this.authService = authService;
  }

  @Override
  public GatewayFilter apply(AuthenticationFilter.Config config) {
    return ((exchange, chain) -> {
      ServerHttpRequest request = exchange.getRequest();

      if(this.routeValidator.isSecured.test(request)) {
        String authorizationHeader = this.extractAuthorizationHeader(request);
        String token = this.extractToken(authorizationHeader);
        ValidatedUserDTO validatedUser = this.authService.validateToken(token);

        request = request.mutate()
          .header("userId", validatedUser.id())
          .header("username", validatedUser.name())
          .header("userEmail", validatedUser.email())
          .build();
      }
      return chain.filter(exchange.mutate().request(request).build());
    });
  }

  private String extractAuthorizationHeader(ServerHttpRequest request) {
    String authorizationHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    if(authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
      throw new MissingAuthException();
    }
    return authorizationHeader;
  }

  private String extractToken(String authorizationHeader) {
    return authorizationHeader.replace("Bearer ", "");
  }

  public static class Config {}
}

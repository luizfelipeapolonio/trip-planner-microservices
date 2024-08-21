package com.felipe.trip_planner_user_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

  private final AuthService authService;
  private final HandlerExceptionResolver resolver;

  public SecurityFilter(AuthService authService, @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
    this.authService = authService;
    this.resolver = resolver;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    try {
      String userId = request.getHeader("userId");
      String username = request.getHeader("username");
      String userEmail = request.getHeader("userEmail");

      if(userId != null && username != null && userEmail != null) {
        UserDetails userDetails = this.authService.loadUserByUsername(userEmail);
        var auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
      }

      filterChain.doFilter(request, response);
    } catch(Exception exception) {
      this.resolver.resolveException(request, response, null, exception);
    }
  }
}

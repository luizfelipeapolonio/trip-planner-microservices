package com.felipe.trip_planner_user_service.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class JwtService {

  @Value("${jwt.key.private}")
  private RSAPrivateKey privateKey;

  @Value("${jwt.key.public}")
  private RSAPublicKey publicKey;

  @Value("${jwt.issuer}")
  private String jwtIssuer;

  public String generateToken(UserPrincipal userPrincipal) {
    try {
      Algorithm algorithm = Algorithm.RSA256(this.publicKey, this.privateKey);
      return JWT.create()
        .withIssuer(this.jwtIssuer)
        .withSubject(userPrincipal.getUsername())
        .withExpiresAt(this.generateExpirationDate())
        .withClaim("userId", userPrincipal.getUser().getId().toString())
        .sign(algorithm);
    } catch(JWTCreationException | IllegalArgumentException e) {
      throw new JWTCreationException("Ocorreu um erro interno do servidor", e);
    }
  }

  public String validateToken(String token) {
    try {
      Algorithm algorithm = Algorithm.RSA256(this.publicKey, this.privateKey);
      JWTVerifier verifier = JWT.require(algorithm).withIssuer(this.jwtIssuer).build();
      DecodedJWT decodedJWT = verifier.verify(token);
      return decodedJWT.getSubject();
    } catch(JWTVerificationException e) {
      throw new JWTVerificationException("O token de acesso fornecido expirou, foi revogado ou é inválido", e);
    }
  }

  private Instant generateExpirationDate() {
    return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
  }
}

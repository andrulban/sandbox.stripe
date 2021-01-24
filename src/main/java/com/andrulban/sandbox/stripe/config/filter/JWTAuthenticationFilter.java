package com.andrulban.sandbox.stripe.config.filter;

import com.andrulban.sandbox.stripe.dto.AuthenticationCredentials;
import com.andrulban.sandbox.stripe.dto.AuthenticationStatus;
import com.andrulban.sandbox.stripe.service.impl.AuthenticationServiceImpl;
import com.andrulban.sandbox.stripe.utils.CustomUserDetails;
import com.andrulban.sandbox.stripe.utils.View;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
  private static final Logger LOGGER = Logger.getLogger(JWTAuthenticationFilter.class.toString());
  private AuthenticationManager authenticationManager;
  private AuthenticationServiceImpl authenticationService;
  private final int expirationTime;
  private final String secret;
  private final String headerName;
  private final String tokenPrefix;

  public JWTAuthenticationFilter(
      AuthenticationManager authenticationManager,
      AuthenticationServiceImpl authenticationService,
      int expirationTime,
      String secret,
      String headerName,
      String tokenPrefix) {
    this.authenticationManager = authenticationManager;
    this.authenticationService = authenticationService;
    this.expirationTime = expirationTime;
    this.secret = secret;
    this.headerName = headerName;
    this.tokenPrefix = tokenPrefix;
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res)
      throws AuthenticationException {
    try {
      // Email and password comes here
      AuthenticationCredentials credentials =
          new ObjectMapper().readValue(req.getInputStream(), AuthenticationCredentials.class);
      AuthenticationStatus status =
          authenticationService.checkUserAuthenticationStatus(credentials.getEmail());
      try {
        if (status.equals(AuthenticationStatus.IS_BLOCKED)) {
          LOGGER.warning(
              "[JWTAuthenticationFilter.attemptAuthentication] User with email: "
                  + credentials.getEmail()
                  + " is blocked!");
          throw new BadCredentialsException(
              "Your account is blocked (10 times unsuccessful authentication), use password reset to unblock your account");
        }
        if (status.equals(AuthenticationStatus.USER_NOT_PRESENT)) {
          LOGGER.warning(
              "[JWTAuthenticationFilter.attemptAuthentication] User with email: "
                  + credentials.getEmail()
                  + " does not exist!");
          throw new BadCredentialsException("Email or password are incorrect!");
        }
        // @Authentication principal value here
        // Email to get user by and password compare to value here
        return authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                new CustomUserDetails(credentials.getEmail(), credentials.getPassword()),
                credentials.getPassword(),
                new ArrayList<>()));
      } catch (AuthenticationException e) {
        // Incorrect attempts increase
        authenticationService.authenticationFailure(credentials.getEmail());
        res.sendError(401, e.getMessage());
        return null;
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void successfulAuthentication(
      HttpServletRequest req, HttpServletResponse res, FilterChain chain, Authentication auth)
      throws IOException {
    CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
    String userJWT =
        new ObjectMapper()
            .disable(MapperFeature.DEFAULT_VIEW_INCLUSION)
            .writerWithView(View.JsonWebToken.class)
            .writeValueAsString(userDetails);

    String token =
        Jwts.builder()
            .setSubject(userJWT)
            .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
            .signWith(SignatureAlgorithm.HS512, secret.getBytes())
            .compact();

    // JWT creation here
    res.addHeader(headerName, tokenPrefix + token);
    // Incorrect attempts reset
    authenticationService.authenticationSuccess(userDetails.getId());
  }
}

package com.andrulban.sandbox.stripe.config;

import com.andrulban.sandbox.stripe.config.filter.JWTAuthenticationFilter;
import com.andrulban.sandbox.stripe.config.filter.JWTAuthorizationFilter;
import com.andrulban.sandbox.stripe.service.impl.AuthenticationServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private UserDetailsService userDetailsService;
  private AuthenticationServiceImpl authenticationService;
  private BCryptPasswordEncoder bCryptPasswordEncoder;
  private final int expirationTime;
  private final String secret;
  private final String headerName;
  private final String tokenPrefix;

  public SecurityConfig(
      UserDetailsService userDetailsService,
      AuthenticationServiceImpl authenticationService,
      BCryptPasswordEncoder bCryptPasswordEncoder,
      @Value("${jwt.secret}") String secret,
      @Value("${jwt.header.name}") String headerName,
      @Value("${jwt.token.prefix}") String tokenPrefix,
      @Value("${jwt.expiration.time}") int expirationTime) {
    this.userDetailsService = userDetailsService;
    this.authenticationService = authenticationService;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    this.expirationTime = expirationTime;
    this.secret = secret;
    this.headerName = headerName;
    this.tokenPrefix = tokenPrefix;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf()
        .disable()
        .authorizeRequests()
        .antMatchers("/**")
        .permitAll()
        .anyRequest()
        .authenticated()
        .and()
        .addFilter(
            new JWTAuthenticationFilter(
                authenticationManager(),
                authenticationService,
                expirationTime,
                secret,
                headerName,
                tokenPrefix))
        .addFilter(
            new JWTAuthorizationFilter(authenticationManager(), secret, headerName, tokenPrefix))
        // this disables session creation on Spring Security
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
  }

  @Override
  public void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userDetailsService) // Is used to get user by userName
        .passwordEncoder(bCryptPasswordEncoder); // Is used to match passwords
  }

  @Override
  public void configure(WebSecurity web) {
    web.ignoring()
        .antMatchers(
            "/users/registration", "/users/password-recovery-mail", "/users/password-reset");
  }
}

package com.andrulban.sandbox.stripe.security;

import com.andrulban.sandbox.stripe.entity.User.UserRole;
import com.andrulban.sandbox.stripe.utils.CustomUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class CustomSecurityContextFactory implements WithSecurityContextFactory<Customer> {

  @Override
  public SecurityContext createSecurityContext(Customer customLocator) {
    SecurityContext context = SecurityContextHolder.createEmptyContext();

    CustomUserDetails principal = new CustomUserDetails();
    principal.setId(1000000L);
    principal.setEmail("fake.customer@gmail.com");
    principal.setUserRole(UserRole.CUSTOMER);
    Authentication auth =
        new UsernamePasswordAuthenticationToken(
            principal, "password", AuthorityUtils.createAuthorityList("ROLE_CUSTOMER"));
    context.setAuthentication(auth);
    return context;
  }
}

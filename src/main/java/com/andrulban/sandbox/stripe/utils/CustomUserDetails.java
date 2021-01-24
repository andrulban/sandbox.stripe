package com.andrulban.sandbox.stripe.utils;

import com.andrulban.sandbox.stripe.entity.User.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
@Setter
public class CustomUserDetails implements UserDetails {

  @JsonView(View.JsonWebToken.class)
  private Long id;

  @JsonView(View.JsonWebToken.class)
  private String email;

  @JsonView(View.JsonWebToken.class)
  private UserRole userRole;

  @JsonView(View.JsonWebToken.class)
  private String firstName;

  @JsonView(View.JsonWebToken.class)
  private String lastName;

  @JsonView(View.JsonWebToken.class)
  private Boolean blocked;

  @JsonIgnore // Not obligatory
  private String password;

  public CustomUserDetails() {}

  public CustomUserDetails(
      Long id,
      String email,
      UserRole userRole,
      String firstName,
      String lastName,
      String password,
      Boolean blocked) {
    this.id = id;
    this.email = email;
    this.userRole = userRole;
    this.firstName = firstName;
    this.lastName = lastName;
    this.password = password;
    this.blocked = blocked;
  }

  public CustomUserDetails(String email, String password) {
    this.email = email;
    this.password = password;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    if (this.getUserRole().equals(UserRole.CUSTOMER)) {
      return AuthorityUtils.createAuthorityList("ROLE_CUSTOMER");
    } else return AuthorityUtils.createAuthorityList("ROLE_UNAUTHENTICATED");
  }

  @Override
  public String getUsername() {
    return getEmail();
  }

  @Override
  public boolean isAccountNonLocked() {
    return !blocked;
  }

  @Override
  public String getPassword() {
    return this.password;
  }

  //
  //    Unused part
  //

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}

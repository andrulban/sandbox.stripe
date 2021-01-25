package com.andrulban.sandbox.stripe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationCredentials {

  private String email;
  private String password;
}

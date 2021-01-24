package com.andrulban.sandbox.stripe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordRecoveryEmailDto {

  @Email(message = "Email is incorrect")
  @NotBlank
  @Size(min = 5, max = 100, message = "email - min 5, max 100 symbols")
  private String email;
}

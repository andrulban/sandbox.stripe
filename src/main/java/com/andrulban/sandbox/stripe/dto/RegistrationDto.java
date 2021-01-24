package com.andrulban.sandbox.stripe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
public class RegistrationDto {

  @Email(message = "Email is incorrect")
  @NotBlank
  @Size(min = 5, max = 100, message = "email - min 5, max 100 symbols")
  private String email;

  @Size(min = 1, max = 50, message = "firsName length from 1 to 50 symbols")
  @NotBlank
  private String firstName;

  @Size(min = 1, max = 50, message = "lastName length from 1 to 50 symbols")
  @NotBlank
  private String lastName;

  @Size(min = 9, max = 10, message = "phoneNumber length from 9 to 10 symbols")
  @NotBlank
  private String phoneNumber;

  @NotNull
  @Size(min = 8, max = 100)
  private String password;
}

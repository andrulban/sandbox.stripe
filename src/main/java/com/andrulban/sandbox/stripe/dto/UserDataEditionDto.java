package com.andrulban.sandbox.stripe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDataEditionDto {

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

  @Size(min = 9, max = 10, message = "phoneNumber length from 7 to 10 symbols")
  @NotBlank
  private String phoneNumber;
}

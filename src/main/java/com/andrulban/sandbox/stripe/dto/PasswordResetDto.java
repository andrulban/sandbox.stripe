package com.andrulban.sandbox.stripe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetDto {

  @Size(min = 8, max = 100, message = "newPassword length from 8 to 100 symbols")
  @NotBlank
  private String newPassword;

  @Size(min = 36, max = 36)
  @NotBlank
  private String token;
}

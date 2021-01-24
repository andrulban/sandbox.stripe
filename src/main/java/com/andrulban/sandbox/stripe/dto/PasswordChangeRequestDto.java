package com.andrulban.sandbox.stripe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordChangeRequestDto {

  @Size(min = 8, max = 100, message = "newPassword length from 8 to 100 symbols")
  @NotBlank
  private String newPassword;

  @Size(min = 8, max = 100, message = "oldPassword length from 8 to 100 symbols")
  @NotBlank
  private String oldPassword;
}

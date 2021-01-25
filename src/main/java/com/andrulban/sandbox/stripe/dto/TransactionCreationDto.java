package com.andrulban.sandbox.stripe.dto;

import com.andrulban.sandbox.stripe.entity.Transaction.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.*;

@Data
@Builder
@AllArgsConstructor
public class TransactionCreationDto {

  @NotNull @Positive private Long amount;

  @NotNull private Currency currency;

  @NotBlank private String stripeToken;

  @NotBlank
  @Size(min = 1, max = 500, message = "Description length should contain from 1 to 500 symbols")
  private String description;

  @Email(message = "Email is incorrect")
  @NotBlank
  private String stripeEmail;
}

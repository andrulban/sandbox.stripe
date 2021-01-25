package com.andrulban.sandbox.stripe.dto;

import com.andrulban.sandbox.stripe.entity.Transaction.Currency;
import com.andrulban.sandbox.stripe.entity.Transaction.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
public class TransactionPreviewDto implements Serializable {

  private long id;

  private String description;

  private String stripeEmail;

  private Currency currency;

  private Long amount;

  private Long fee;

  private Status status;

  protected Date creationDate;
}

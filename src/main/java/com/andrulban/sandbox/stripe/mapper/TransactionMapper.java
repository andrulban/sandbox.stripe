package com.andrulban.sandbox.stripe.mapper;

import com.andrulban.sandbox.stripe.dto.TransactionPreviewDto;
import com.andrulban.sandbox.stripe.entity.Transaction;

// TODO: use MapStruct instead of manual mapper
public class TransactionMapper {

  public static TransactionPreviewDto mapToPreviewDto(Transaction transaction) {
    return TransactionPreviewDto.builder()
        .id(transaction.getId())
        .description(transaction.getDescription())
        .stripeEmail(transaction.getStripeEmail())
        .currency(transaction.getCurrency())
        .amount(transaction.getAmount())
        .fee(transaction.getFee())
        .status(transaction.getStatus())
        .creationDate(transaction.getCreationDate())
        .build();
  }
}

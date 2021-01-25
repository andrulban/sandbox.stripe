package com.andrulban.sandbox.stripe.service;

import com.andrulban.sandbox.stripe.dto.Page;
import com.andrulban.sandbox.stripe.dto.TransactionCreationDto;
import com.andrulban.sandbox.stripe.dto.TransactionPreviewDto;
import com.andrulban.sandbox.stripe.dto.predicate.TransactionFilteringPredicate;

public interface TransactionService {

  Page<TransactionPreviewDto> filterTransactions(
      TransactionFilteringPredicate transactionFilteringPredicate, long userId);

  long processTransaction(TransactionCreationDto transactionCreationDto, long userId);
}

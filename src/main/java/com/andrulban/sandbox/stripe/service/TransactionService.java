package com.andrulban.sandbox.stripe.service;

import com.andrulban.sandbox.stripe.dto.TransactionCreationDto;

public interface TransactionService {

  long processTransaction(TransactionCreationDto transactionCreationDto, long userId);
}

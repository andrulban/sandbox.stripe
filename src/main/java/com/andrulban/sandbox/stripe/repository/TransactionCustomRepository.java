package com.andrulban.sandbox.stripe.repository;

import com.andrulban.sandbox.stripe.entity.Transaction;

import java.util.List;
import java.util.Map;

public interface TransactionCustomRepository {

  List<Transaction> getTransactionsByFilter(
      Map<String, Object> filters,
      String field,
      boolean isAscending,
      Integer firstResult,
      Integer maxResults);

  Long countTransactionsByFilter(Map<String, Object> filters);
}

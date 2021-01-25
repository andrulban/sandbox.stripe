package com.andrulban.sandbox.stripe.dto.predicate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionFilteringPredicate {

  private String description;
  private Long amount;
  private Long amountFrom;
  private Long amountTo;

  private String sortField;
  private boolean ascending;
  private Integer firstResult;
  private Integer maxResults;

  public Map<String, Object> getNotNullParams() {
    Map<String, Object> queryParamMap = new HashMap<>();
    if (getDescription() != null) {
      queryParamMap.put("description", getDescription());
    }
    if (getAmount() != null) {
      queryParamMap.put("amount", getAmount());
    }
    if (getAmountFrom() != null) {
      queryParamMap.put("amountFrom", getAmountFrom());
    }
    if (getAmountTo() != null) {
      queryParamMap.put("amountTo", getAmountTo());
    }

    return queryParamMap;
  }
}

package com.andrulban.sandbox.stripe.service.impl;

import com.andrulban.sandbox.stripe.dto.Page;
import com.andrulban.sandbox.stripe.dto.TransactionCreationDto;
import com.andrulban.sandbox.stripe.dto.TransactionPreviewDto;
import com.andrulban.sandbox.stripe.dto.predicate.TransactionFilteringPredicate;
import com.andrulban.sandbox.stripe.entity.Transaction;
import com.andrulban.sandbox.stripe.entity.Transaction.Status;
import com.andrulban.sandbox.stripe.entity.User;
import com.andrulban.sandbox.stripe.exception.ApiException;
import com.andrulban.sandbox.stripe.exception.ExceptionType;
import com.andrulban.sandbox.stripe.mapper.TransactionMapper;
import com.andrulban.sandbox.stripe.repository.TransactionRepository;
import com.andrulban.sandbox.stripe.repository.UserRepository;
import com.andrulban.sandbox.stripe.service.ChargeService;
import com.andrulban.sandbox.stripe.service.TransactionService;
import com.google.common.collect.ImmutableMap;
import com.stripe.Stripe;
import com.stripe.exception.*;
import com.stripe.model.Charge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

  private static final Logger LOGGER = LogManager.getLogger(TransactionServiceImpl.class);

  private final ChargeService chargeService;
  private final TransactionRepository transactionRepository;
  private final UserRepository userRepository;

  public TransactionServiceImpl(
      @Value("${stripe.private.key}") String stripePk,
      ChargeService chargeService,
      TransactionRepository transactionRepository,
      UserRepository userRepository) {
    Stripe.apiKey = stripePk;
    this.chargeService = chargeService;
    this.transactionRepository = transactionRepository;
    this.userRepository = userRepository;
  }

  @Override
  public Page<TransactionPreviewDto> filterTransactions(
      TransactionFilteringPredicate filteringPredicate, long userId) {
    Map<String, Object> filteringMap = filteringPredicate.getNotNullParams();
    filteringMap.put("userId", userId);

    List<TransactionPreviewDto> content =
        transactionRepository
            .getTransactionsByFilter(
                filteringMap,
                filteringPredicate.getSortField(),
                filteringPredicate.isAscending(),
                filteringPredicate.getFirstResult(),
                filteringPredicate.getMaxResults())
            .stream()
            .map(TransactionMapper::mapToPreviewDto)
            .collect(Collectors.toList());

    Long totalElements = transactionRepository.countTransactionsByFilter(filteringMap);

    return new Page<>(content, totalElements);
  }

  @Cacheable(cacheNames = "transactionByIdCache", key = "{#id, #userId}")
  @Override
  public TransactionPreviewDto getTransactionById(long id, long userId) {
    return transactionRepository
        .findById(id)
        .filter(t -> t.getUserId().equals(userId))
        .map(TransactionMapper::mapToPreviewDto)
        .orElseThrow(
            () ->
                new ApiException(
                    "Transaction with id: " + id + " does not exist!", ExceptionType.NO_RESULT));
  }

  @Override
  public long processTransaction(TransactionCreationDto transactionCreationDto, long userId) {
    Transaction transaction = saveAndFlushTransaction(transactionCreationDto, userId);

    ImmutableMap<String, Object> chargeParams = createChargeParamsMap(transactionCreationDto);

    Charge charge = null;
    ExceptionType exceptionType = null;
    String apiExceptionMessage = null;
    String customerExceptionMessage = "Error during transaction processing with Stripe";

    try {
      charge = chargeService.charge(chargeParams);
    } catch (AuthenticationException
        | InvalidRequestException
        | APIConnectionException
        | APIException e) {
      LOGGER.warn(e.getCause());

      exceptionType = ExceptionType.ERROR;
      apiExceptionMessage =
          String.format("Error message: %s, with cause %s.", e.getMessage(), e.getCause());
    } catch (CardException e) {
      LOGGER.warn(
          String.format(
              "CardException with message: %s and code: %s", e.getMessage(), e.getCode()));
      LOGGER.warn(e.getCause());

      exceptionType = ExceptionType.VALIDATION_ERROR;
      apiExceptionMessage =
          String.format("Error message: %s, with cause %s.", e.getMessage(), e.getCause());
      customerExceptionMessage += e.getMessage();
    }

    if (exceptionType != null) {
      updateWithExceptionDetails(transaction, apiExceptionMessage);
      throw new ApiException(customerExceptionMessage, exceptionType);
    }

    updateWithSuccessDetails(transaction, charge);
    return transaction.getId();
  }

  private Transaction saveAndFlushTransaction(
      TransactionCreationDto transactionCreationDto, long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () ->
                    new ApiException(
                        String.format("User with id: %s does not exist", userId),
                        ExceptionType.VALIDATION_ERROR));

    Transaction transaction =
        Transaction.builder()
            .description(transactionCreationDto.getDescription())
            .amount(transactionCreationDto.getAmount())
            .currency(transactionCreationDto.getCurrency())
            .stripeToken(transactionCreationDto.getStripeToken())
            .stripeEmail(transactionCreationDto.getStripeEmail())
            .status(Status.NEW)
            .user(user)
            .build();
    transactionRepository.saveAndFlush(transaction);

    return transaction;
  }

  private static ImmutableMap<String, Object> createChargeParamsMap(
      TransactionCreationDto transactionCreationDto) {
    ImmutableMap.Builder<String, Object> chargeParams = new ImmutableMap.Builder<>();
    chargeParams.put("amount", transactionCreationDto.getAmount());
    chargeParams.put("currency", transactionCreationDto.getCurrency().name());
    chargeParams.put("description", transactionCreationDto.getDescription());
    chargeParams.put("source", transactionCreationDto.getStripeToken());

    return chargeParams.build();
  }

  private void updateWithExceptionDetails(Transaction transaction, String apiExceptionMessage) {
    transaction.setStatus(Status.ERROR);
    transaction.setErrorMessage(apiExceptionMessage);
    transactionRepository.saveAndFlush(transaction);
  }

  private void updateWithSuccessDetails(Transaction transaction, Charge charge) {
    transaction.setStatus(Status.SUCCESS);
    transaction.setStripeId(charge.getId());
    transaction.setStripeStatus(charge.getStatus());
    transaction.setFee(charge.getBalanceTransactionObject().getFee());
    // TODO: test catching exception in case of save failure to be able to return successful
    //  response
    transactionRepository.saveAndFlush(transaction);
  }
}

package com.andrulban.sandbox.stripe.service.impl;

import com.andrulban.sandbox.stripe.dto.Page;
import com.andrulban.sandbox.stripe.dto.TransactionCreationDto;
import com.andrulban.sandbox.stripe.dto.TransactionPreviewDto;
import com.andrulban.sandbox.stripe.dto.predicate.TransactionFilteringPredicate;
import com.andrulban.sandbox.stripe.entity.Transaction;
import com.andrulban.sandbox.stripe.entity.Transaction.Status;
import com.andrulban.sandbox.stripe.entity.User;
import com.andrulban.sandbox.stripe.exception.ApiException;
import com.andrulban.sandbox.stripe.repository.TransactionRepository;
import com.andrulban.sandbox.stripe.repository.UserRepository;
import com.andrulban.sandbox.stripe.service.ChargeService;
import com.google.common.collect.ImmutableMap;
import com.stripe.exception.APIException;
import com.stripe.exception.CardException;
import com.stripe.model.BalanceTransaction;
import com.stripe.model.Charge;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.HashMap;
import java.util.Optional;

import static com.andrulban.sandbox.stripe.entity.Transaction.Currency.EUR;
import static com.andrulban.sandbox.stripe.entity.Transaction.Status.SUCCESS;
import static com.andrulban.sandbox.stripe.exception.ExceptionType.ERROR;
import static com.andrulban.sandbox.stripe.exception.ExceptionType.VALIDATION_ERROR;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public class TransactionServiceImplTest {

  @Rule public final MockitoRule mockitoRule = MockitoJUnit.rule();

  private TransactionCreationDto transactionCreationDto;

  @Mock private ChargeService chargeService;
  @Mock private TransactionRepository transactionRepository;
  @Mock private UserRepository userRepository;

  TransactionServiceImpl transactionService;

  @Before
  public void setUp() {
    transactionService =
        new TransactionServiceImpl(
            "stripePk", chargeService, transactionRepository, userRepository);

    transactionCreationDto =
        TransactionCreationDto.builder()
            .description("description")
            .amount(300L)
            .currency(EUR)
            .stripeToken("token")
            .stripeEmail("stripe@gmail.com")
            .build();
  }

  @Test
  public void filterTransactions_mapsAllNotNullParamsFromPredicateAndAddsUserId() {
    // Arrange
    TransactionFilteringPredicate filteringPredicate =
        TransactionFilteringPredicate.builder()
            .description("de")
            .amount(1L)
            .amountFrom(1L)
            .amountTo(1L)
            .build();

    HashMap<String, Object> filteringMap = new HashMap<>();
    filteringMap.put("description", "de");
    filteringMap.put("amount", 1L);
    filteringMap.put("amountFrom", 1L);
    filteringMap.put("amountTo", 1L);
    filteringMap.put("userId", 1L);

    when(transactionRepository.countTransactionsByFilter(eq(filteringMap))).thenReturn(0L);

    // Act
    Page<TransactionPreviewDto> result =
        transactionService.filterTransactions(filteringPredicate, 1L);

    // Assert
    assertThat(result.getContent()).isEmpty();
    assertThat(result.getTotalElements()).isEqualTo(0L);
  }

  @Test
  public void processTransaction_success() throws Exception {
    // Arrange
    when(userRepository.findById(any())).thenReturn(Optional.of(User.builder().id(1L).build()));

    Charge charge = new Charge();
    charge.setId("stripeId");
    charge.setStatus("stripeStatus");
    BalanceTransaction balanceTransaction = new BalanceTransaction();
    balanceTransaction.setFee(10L);
    charge.setBalanceTransactionObject(balanceTransaction);
    when(chargeService.charge(any())).thenReturn(charge);

    // Act
    transactionService.processTransaction(transactionCreationDto, 1L);

    // Assert
    ImmutableMap.Builder<String, Object> chargeParams = new ImmutableMap.Builder<>();
    chargeParams.put("amount", transactionCreationDto.getAmount());
    chargeParams.put("currency", transactionCreationDto.getCurrency().name());
    chargeParams.put("description", transactionCreationDto.getDescription());
    chargeParams.put("source", transactionCreationDto.getStripeToken());
    verify(chargeService, times(1)).charge(eq(chargeParams.build()));

    ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
    verify(transactionRepository, times(2)).saveAndFlush(transactionCaptor.capture());

    Transaction initialTransaction = transactionCaptor.getAllValues().get(0);
    assertThat(initialTransaction.getDescription())
        .isEqualTo(transactionCreationDto.getDescription());
    assertThat(initialTransaction.getAmount()).isEqualTo(transactionCreationDto.getAmount());
    assertThat(initialTransaction.getCurrency()).isEqualTo(transactionCreationDto.getCurrency());
    assertThat(initialTransaction.getStripeToken())
        .isEqualTo(transactionCreationDto.getStripeToken());
    assertThat(initialTransaction.getStripeEmail())
        .isEqualTo(transactionCreationDto.getStripeEmail());
    assertThat(initialTransaction.getStatus()).isEqualTo(SUCCESS);

    Transaction finalTransaction = transactionCaptor.getAllValues().get(1);
    assertThat(finalTransaction.getStripeId()).isEqualTo(charge.getId());
    assertThat(finalTransaction.getStripeStatus()).isEqualTo(charge.getStatus());
    assertThat(finalTransaction.getFee()).isEqualTo(charge.getBalanceTransactionObject().getFee());
    assertThat(finalTransaction.getErrorMessage()).isNull();
  }

  @Test
  public void processTransaction_userNotFound_throwsValidationErrorEx() {
    when(userRepository.findById(any())).thenReturn(Optional.empty());

    ApiException thrown =
        assertThrows(
            ApiException.class,
            () -> transactionService.processTransaction(transactionCreationDto, 1L));

    assertThat(thrown.getStatus()).isEqualTo(VALIDATION_ERROR);
  }

  @Test
  public void processTransaction_chargeServiceThrowsTechnicalException_throwsInternalErrorEx()
      throws Exception {
    // Arrange
    when(userRepository.findById(any())).thenReturn(Optional.of(User.builder().id(1L).build()));
    when(chargeService.charge(any()))
        .thenThrow(
            new APIException(
                "customer message", "requestId", 400, new RuntimeException("technical message")));

    // Act
    ApiException thrown =
        assertThrows(
            ApiException.class,
            () -> transactionService.processTransaction(transactionCreationDto, 1L));

    // Assert
    assertThat(thrown.getStatus()).isEqualTo(ERROR);
    assertThat(thrown.getMessage()).contains("Error during transaction processing with Stripe");

    ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
    verify(transactionRepository, times(2)).saveAndFlush(transactionCaptor.capture());
    Transaction savedTransaction = transactionCaptor.getValue();

    assertThat(savedTransaction.getStatus()).isEqualTo(Status.ERROR);
    assertThat(savedTransaction.getErrorMessage()).contains("technical message");
  }

  @Test
  public void processTransaction_chargeServiceThrowsCardException_throwsValidationErrorEx()
      throws Exception {
    // Arrange
    when(userRepository.findById(any())).thenReturn(Optional.of(User.builder().id(1L).build()));
    when(chargeService.charge(any()))
        .thenThrow(
            new CardException(
                "customer message",
                "requestId",
                "code",
                "param",
                "declineCode",
                "charge",
                400,
                new RuntimeException("technical message")));

    // Act
    ApiException thrown =
        assertThrows(
            ApiException.class,
            () -> transactionService.processTransaction(transactionCreationDto, 1L));

    // Assert
    assertThat(thrown.getStatus()).isEqualTo(VALIDATION_ERROR);
    assertThat(thrown.getMessage()).contains("customer message");

    ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
    verify(transactionRepository, times(2)).saveAndFlush(transactionCaptor.capture());
    Transaction savedTransaction = transactionCaptor.getValue();

    assertThat(savedTransaction.getStatus()).isEqualTo(Status.ERROR);
    assertThat(savedTransaction.getErrorMessage()).contains("technical message");
  }
}

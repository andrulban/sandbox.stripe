package com.andrulban.sandbox.stripe.controller;

import com.andrulban.sandbox.stripe.dto.TransactionCreationDto;
import com.andrulban.sandbox.stripe.entity.Transaction;
import com.andrulban.sandbox.stripe.repository.TransactionRepository;
import com.andrulban.sandbox.stripe.security.Customer;
import com.andrulban.sandbox.stripe.service.ChargeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.APIException;
import com.stripe.model.BalanceTransaction;
import com.stripe.model.Charge;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static com.andrulban.sandbox.stripe.entity.Transaction.Currency.EUR;
import static com.andrulban.sandbox.stripe.entity.Transaction.Status.ERROR;
import static com.andrulban.sandbox.stripe.entity.Transaction.Status.SUCCESS;
import static com.google.common.truth.Truth.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Requires running docker-compose because instead of in-memory DB, DB form docker-compose is used
 * to make tests run faster.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:sql/user.sql")
public class TransactionRestControllerTest {

  private static final String STRIPE_TOKEN = "token";

  @MockBean private ChargeService chargeService;

  @Autowired private ObjectMapper objectMapper;
  @Autowired private WebApplicationContext context;
  @Autowired private TransactionRepository transactionRepository;

  private MockMvc mvc;
  private TransactionCreationDto transactionCreationDto;

  @Before
  public void setUp() {
    mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

    transactionCreationDto =
        TransactionCreationDto.builder()
            .description("description")
            .amount(300L)
            .currency(EUR)
            .stripeToken(STRIPE_TOKEN)
            .stripeEmail("stripe@gmail.com")
            .build();
  }

  @Test
  @Customer
  public void processTransaction_success() throws Exception {
    // Arrange
    Charge charge = createCharge();
    when(chargeService.charge(any())).thenReturn(charge);

    // Act
    MvcResult transactionCreationResult =
        mvc.perform(
                post("/transactions")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(transactionCreationDto)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andReturn();

    // Assert
    String locationHeader = transactionCreationResult.getResponse().getHeader("Location");
    String transactionId = locationHeader.split("/")[2];

    Transaction createTransaction =
        transactionRepository
            .findById(Long.valueOf(transactionId))
            .orElseThrow(AssertionError::new);

    assertThat(createTransaction.getDescription())
        .isEqualTo(transactionCreationDto.getDescription());
    assertThat(createTransaction.getAmount()).isEqualTo(transactionCreationDto.getAmount());
    assertThat(createTransaction.getCurrency()).isEqualTo(transactionCreationDto.getCurrency());
    assertThat(createTransaction.getStripeToken())
        .isEqualTo(transactionCreationDto.getStripeToken());
    assertThat(createTransaction.getStripeEmail())
        .isEqualTo(transactionCreationDto.getStripeEmail());
    assertThat(createTransaction.getStatus()).isEqualTo(SUCCESS);
    assertThat(createTransaction.getErrorMessage()).isNull();
    assertThat(createTransaction.getStripeId()).isEqualTo(charge.getId());
    assertThat(createTransaction.getStripeStatus()).isEqualTo(charge.getStatus());
    assertThat(createTransaction.getFee()).isEqualTo(charge.getBalanceTransactionObject().getFee());
  }

  @Test
  @Customer
  public void processTransaction_chargeFailsWithTechExc_writesInitialInfoIntoDbAndThrowsException()
      throws Exception {
    // Arrange
    when(chargeService.charge(any()))
        .thenThrow(
            new APIException(
                "customer message", "requestId", 400, new RuntimeException("technical message")));

    // Act
    mvc.perform(
            post("/transactions")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transactionCreationDto)))
        .andDo(print())
        .andExpect(status().isInternalServerError())
        .andExpect(
            jsonPath(
                "$.message", containsString("Error during transaction processing with Stripe")));

    // Assert
    Transaction createTransaction =
        transactionRepository.findByStripeToken(STRIPE_TOKEN).orElseThrow(AssertionError::new);

    assertThat(createTransaction.getDescription())
        .isEqualTo(transactionCreationDto.getDescription());
    assertThat(createTransaction.getAmount()).isEqualTo(transactionCreationDto.getAmount());
    assertThat(createTransaction.getCurrency()).isEqualTo(transactionCreationDto.getCurrency());
    assertThat(createTransaction.getStripeToken())
        .isEqualTo(transactionCreationDto.getStripeToken());
    assertThat(createTransaction.getStripeEmail())
        .isEqualTo(transactionCreationDto.getStripeEmail());
    assertThat(createTransaction.getStatus()).isEqualTo(ERROR);
    assertThat(createTransaction.getErrorMessage()).contains("technical message");
  }

  private static Charge createCharge() {
    Charge charge = new Charge();
    charge.setId("stripeId");
    charge.setStatus("stripeStatus");
    BalanceTransaction balanceTransaction = new BalanceTransaction();
    balanceTransaction.setFee(10L);
    charge.setBalanceTransactionObject(balanceTransaction);
    return charge;
  }
}

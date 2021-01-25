package com.andrulban.sandbox.stripe.controller;

import com.andrulban.sandbox.stripe.dto.Page;
import com.andrulban.sandbox.stripe.dto.TransactionCreationDto;
import com.andrulban.sandbox.stripe.dto.TransactionPreviewDto;
import com.andrulban.sandbox.stripe.dto.predicate.TransactionFilteringPredicate;
import com.andrulban.sandbox.stripe.service.TransactionService;
import com.andrulban.sandbox.stripe.utils.CustomUserDetails;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/transactions")
public class TransactionRestController {

  private final TransactionService transactionService;

  public TransactionRestController(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  @PreAuthorize("hasAnyRole('CUSTOMER')")
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Page<TransactionPreviewDto>> filterTransactions(
      TransactionFilteringPredicate filteringPredicate, Authentication authentication) {
    CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
    return ResponseEntity.ok(
        transactionService.filterTransactions(filteringPredicate, customUserDetails.getId()));
  }

  @PreAuthorize("hasAnyRole('CUSTOMER')")
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity processTransaction(
      @Valid @RequestBody TransactionCreationDto transactionCreationDto,
      Authentication authentication) {
    CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
    long transactionId =
        transactionService.processTransaction(transactionCreationDto, customUserDetails.getId());
    return ResponseEntity.created(URI.create("/transactions/" + transactionId)).build();
  }
}

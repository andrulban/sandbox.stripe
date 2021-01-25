package com.andrulban.sandbox.stripe.repository;

import com.andrulban.sandbox.stripe.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository
    extends JpaRepository<Transaction, Long>, TransactionCustomRepository {
  Optional<Transaction> findByStripeToken(String stripeToken);
}

package com.andrulban.sandbox.stripe.repository;

import com.andrulban.sandbox.stripe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmailIgnoreCase(String email);

  Optional<User> findByResetToken(String resetToken);
}

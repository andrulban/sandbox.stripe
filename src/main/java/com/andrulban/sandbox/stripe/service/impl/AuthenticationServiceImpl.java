package com.andrulban.sandbox.stripe.service.impl;

import com.andrulban.sandbox.stripe.dto.AuthenticationStatus;
import com.andrulban.sandbox.stripe.entity.User;
import com.andrulban.sandbox.stripe.repository.UserRepository;
import com.andrulban.sandbox.stripe.utils.CustomUserDetails;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Primary
public class AuthenticationServiceImpl implements UserDetailsService {
  private final UserRepository userRepository;

  public AuthenticationServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user =
        userRepository
            .findByEmailIgnoreCase(email)
            .orElseThrow(
                () ->
                    new UsernameNotFoundException("User with email: " + email + "does not exist!"));
    return new CustomUserDetails(
        user.getId(),
        user.getEmail(),
        user.getUserRole(),
        user.getFirstName(),
        user.getLastName(),
        user.getPassword(),
        user.isBlocked());
  }

  public AuthenticationStatus checkUserAuthenticationStatus(String userEmail) {
    Optional<User> user = userRepository.findByEmailIgnoreCase(userEmail);
    if (user.isEmpty()) {
      return AuthenticationStatus.USER_NOT_PRESENT;
    }
    if (user.get().isBlocked()) {
      return AuthenticationStatus.IS_BLOCKED;
    }
    return AuthenticationStatus.SUCCESS;
  }

  public void authenticationFailure(String userEmail) {
    userRepository
        .findByEmailIgnoreCase(userEmail)
        .ifPresent(
            user -> {
              user.setIncorrectLoginAttempts(user.getIncorrectLoginAttempts() + 1);
              if (user.getIncorrectLoginAttempts() > 9) {
                user.setBlocked(true);
              }
              userRepository.saveAndFlush(user);
            });
  }

  public void authenticationSuccess(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () ->
                    new UsernameNotFoundException("User with id: " + userId + " does not exist!"));

    user.setIncorrectLoginAttempts(0);
    userRepository.save(user);
  }
}

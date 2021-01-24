package com.andrulban.sandbox.stripe.service.impl;

import com.andrulban.sandbox.stripe.dto.AuthenticationStatus;
import com.andrulban.sandbox.stripe.entity.User;
import com.andrulban.sandbox.stripe.repository.UserRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static com.andrulban.sandbox.stripe.dto.AuthenticationStatus.*;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class AuthenticationServiceImplTest {

  @Rule public final MockitoRule mockitoRule = MockitoJUnit.rule();

  private static final String USER_EMAIL = "fake@gmail.com";

  @Mock private UserRepository userRepository;

  private AuthenticationServiceImpl authenticationService;

  @Before
  public void setUp() {
    authenticationService = new AuthenticationServiceImpl(userRepository);
  }

  @Test
  public void loadUserByUsername_success() {
    when(userRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.of(new User()));

    UserDetails userDetails = authenticationService.loadUserByUsername(USER_EMAIL);

    assertThat(userDetails).isNotNull();
  }

  @Test
  public void loadUserByUsername_notFound_throwsException() {
    when(userRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.empty());

    assertThrows(
        UsernameNotFoundException.class,
        () -> authenticationService.loadUserByUsername(USER_EMAIL));
  }

  @Test
  public void checkUserAuthenticationStatus_noIssues_returnsSuccessStatus() {
    when(userRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.of(new User()));

    AuthenticationStatus status = authenticationService.checkUserAuthenticationStatus(USER_EMAIL);

    assertThat(status).isEqualTo(SUCCESS);
  }

  @Test
  public void checkUserAuthenticationStatus_notFound_returnsNotPresentStatus() {
    when(userRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.empty());

    AuthenticationStatus status = authenticationService.checkUserAuthenticationStatus(USER_EMAIL);

    assertThat(status).isEqualTo(USER_NOT_PRESENT);
  }

  @Test
  public void checkUserAuthenticationStatus_userIsBlocked_returnsNotPresentStatus() {
    User blockedUser = new User();
    blockedUser.setBlocked(true);
    when(userRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.of(blockedUser));

    AuthenticationStatus status = authenticationService.checkUserAuthenticationStatus(USER_EMAIL);

    assertThat(status).isEqualTo(IS_BLOCKED);
  }

  @Test
  public void authenticationFailure_notFound_doesNothing() {
    when(userRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.empty());

    authenticationService.authenticationFailure(USER_EMAIL);
  }

  @Test
  public void authenticationFailure_foundLessThanNineIncorrectLoginAttempts_incrementsCount() {
    User user = new User();
    user.setId(1L);
    user.setBlocked(false);
    user.setIncorrectLoginAttempts(1);
    when(userRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.of(user));

    authenticationService.authenticationFailure(USER_EMAIL);

    assertThat(user.getIncorrectLoginAttempts()).isEqualTo(2);
    assertThat(user.isBlocked()).isFalse();
  }

  @Test
  public void
      authenticationFailure_foundMoreThanNineIncorrectLoginAttempts_incrementsCountAndBlocks() {
    User user = new User();
    user.setId(1L);
    user.setBlocked(false);
    user.setIncorrectLoginAttempts(9);
    when(userRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.of(user));

    authenticationService.authenticationFailure(USER_EMAIL);

    assertThat(user.getIncorrectLoginAttempts()).isEqualTo(10);
    assertThat(user.isBlocked()).isTrue();
  }

  @Test
  public void authenticationSuccess_success_nullifiesIncorrectLogicAttempts() {
    User user = new User();
    user.setId(1L);
    user.setIncorrectLoginAttempts(3);
    when(userRepository.findById(any())).thenReturn(Optional.of(user));

    authenticationService.authenticationSuccess(1L);

    assertThat(user.getIncorrectLoginAttempts()).isEqualTo(0);
  }

  @Test
  public void authenticationSuccess_notFound_throwsException() {
    when(userRepository.findById(any())).thenReturn(Optional.empty());

    assertThrows(
        UsernameNotFoundException.class, () -> authenticationService.authenticationSuccess(1L));
  }
}

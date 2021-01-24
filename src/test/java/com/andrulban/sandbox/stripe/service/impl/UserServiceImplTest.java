package com.andrulban.sandbox.stripe.service.impl;

import com.andrulban.sandbox.stripe.dto.*;
import com.andrulban.sandbox.stripe.entity.User;
import com.andrulban.sandbox.stripe.exception.ApiException;
import com.andrulban.sandbox.stripe.repository.UserRepository;
import com.andrulban.sandbox.stripe.service.EmailService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static com.andrulban.sandbox.stripe.entity.User.UserRole.CUSTOMER;
import static com.andrulban.sandbox.stripe.exception.ExceptionType.NO_RESULT;
import static com.andrulban.sandbox.stripe.exception.ExceptionType.VALIDATION_ERROR;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public class UserServiceImplTest {

  @Rule public final MockitoRule mockitoRule = MockitoJUnit.rule();

  // Encoded "password" string
  private static final String PASSWORD_HASH =
      "$2a$10$0Ld/pnah.M6jxHWSDYY.J.WI7SZnaA0V2VjXREWtddFXUIT1rEVLm";
  private static final String BCRYPT_ENCODING_PREFIX = "$2a$10$";

  @Mock private UserRepository userRepository;
  @Mock private EmailService emailService;

  private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
  private UserServiceImpl userService;

  @Before
  public void setUp() {
    userService = new UserServiceImpl(passwordEncoder, userRepository, emailService);
  }

  // TODO: add notification email if email has been updated
  @Test
  public void registerUser_success() {
    when(userRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.empty());
    RegistrationDto registrationDto =
        RegistrationDto.builder()
            .email("fake@gmail.com")
            .firstName("a")
            .lastName("a")
            .phoneNumber("2")
            .password("12345678")
            .build();

    userService.registerUser(registrationDto);

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository, times(1)).save(userCaptor.capture());
    User savedUser = userCaptor.getValue();
    assertThat(savedUser.getEmail()).isEqualTo(registrationDto.getEmail());
    assertThat(savedUser.getFirstName()).isEqualTo(registrationDto.getFirstName());
    assertThat(savedUser.getLastName()).isEqualTo(registrationDto.getLastName());
    assertThat(savedUser.getPhoneNumber()).isEqualTo(registrationDto.getPhoneNumber());
    assertThat(savedUser.getPassword()).startsWith(BCRYPT_ENCODING_PREFIX);
    assertThat(savedUser.getUserRole()).isEqualTo(CUSTOMER);
  }

  @Test
  public void registerUser_notFound_throwsException() {
    when(userRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.of(new User()));

    ApiException thrown =
        assertThrows(
            ApiException.class,
            () ->
                userService.registerUser(
                    RegistrationDto.builder().email("fake@gmail.com").build()));

    assertThat(thrown.getStatus()).isEqualTo(VALIDATION_ERROR);
  }

  @Test
  public void getUserById_success() {
    User dbUser =
        User.builder()
            .id(1L)
            .email("f@gmail.com")
            .firstName("a")
            .lastName("a")
            .phoneNumber("2")
            .build();
    when(userRepository.findById(any())).thenReturn(Optional.of(dbUser));

    UserDto found = userService.getUserById(1L);

    assertThat(found.getId()).isEqualTo(dbUser.getId());
    assertThat(found.getEmail()).isEqualTo(dbUser.getEmail());
    assertThat(found.getFirstName()).isEqualTo(dbUser.getFirstName());
    assertThat(found.getLastName()).isEqualTo(dbUser.getLastName());
    assertThat(found.getPhoneNumber()).isEqualTo(dbUser.getPhoneNumber());
    assertThat(found.getUserRole()).isEqualTo(dbUser.getUserRole());
  }

  @Test
  public void getUserById_notFound_throwsException() {
    when(userRepository.findById(any())).thenReturn(Optional.empty());

    ApiException thrown = assertThrows(ApiException.class, () -> userService.getUserById(1L));

    assertThat(thrown.getStatus()).isEqualTo(NO_RESULT);
  }

  @Test
  public void editUserData_notFound_success() {
    User dbUser =
        User.builder()
            .id(1L)
            .email("f@gmail.com")
            .firstName("a")
            .lastName("a")
            .phoneNumber("2")
            .build();
    when(userRepository.findById(any())).thenReturn(Optional.of(dbUser));
    UserDataEditionDto updateRequest =
        UserDataEditionDto.builder()
            .email("fake@gmail.com")
            .firstName("f")
            .lastName("l")
            .phoneNumber("1")
            .build();

    userService.editUserData(1L, updateRequest);

    assertThat(dbUser.getEmail()).isEqualTo(updateRequest.getEmail());
    assertThat(dbUser.getFirstName()).isEqualTo(updateRequest.getFirstName());
    assertThat(dbUser.getLastName()).isEqualTo(updateRequest.getLastName());
    assertThat(dbUser.getPhoneNumber()).isEqualTo(updateRequest.getPhoneNumber());
  }

  @Test
  public void editUserData_notFound_throwsException() {
    when(userRepository.findById(any())).thenReturn(Optional.empty());

    ApiException thrown =
        assertThrows(
            ApiException.class, () -> userService.editUserData(1L, new UserDataEditionDto()));

    assertThat(thrown.getStatus()).isEqualTo(NO_RESULT);
  }

  @Test
  public void editUserData_attemptToEditSomeoneElseData_throwsException() {
    User foundByAuthorizedIdUser = User.builder().id(1L).email("fake@gmail.com").build();
    when(userRepository.findById(any())).thenReturn(Optional.of(foundByAuthorizedIdUser));
    String someoneElseEmail = "someone_else_account@gmail.com";
    User foundByEmailUser = User.builder().id(2L).email(someoneElseEmail).build();
    when(userRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.of(foundByEmailUser));

    ApiException thrown =
        assertThrows(
            ApiException.class,
            () ->
                userService.editUserData(
                    1L, UserDataEditionDto.builder().email(someoneElseEmail).build()));

    assertThat(thrown.getStatus()).isEqualTo(VALIDATION_ERROR);
  }

  @Test
  public void changePassword_success() {
    User dbUser = User.builder().id(1L).password(PASSWORD_HASH).build();
    when(userRepository.findById(any())).thenReturn(Optional.of(dbUser));
    PasswordChangeRequestDto passwordChangeRequestDto =
        PasswordChangeRequestDto.builder().newPassword("newPass").oldPassword("password").build();

    userService.changePassword(1L, passwordChangeRequestDto);

    assertThat(dbUser.getPassword()).isNotEqualTo(PASSWORD_HASH);
    assertThat(dbUser.getPassword()).startsWith(BCRYPT_ENCODING_PREFIX);
  }

  @Test
  public void changePassword_notFound_throwsException() {
    when(userRepository.findById(any())).thenReturn(Optional.empty());

    ApiException thrown =
        assertThrows(
            ApiException.class,
            () -> userService.changePassword(1L, new PasswordChangeRequestDto()));

    assertThat(thrown.getStatus()).isEqualTo(NO_RESULT);
  }

  @Test
  public void changePassword_oldPasswordIsWrong_throwsException() {
    when(userRepository.findById(any()))
        .thenReturn(Optional.of(User.builder().id(1L).password(PASSWORD_HASH).build()));
    PasswordChangeRequestDto passwordChangeRequestDto =
        PasswordChangeRequestDto.builder().newPassword("newPass").oldPassword("not_pass").build();

    ApiException thrown =
        assertThrows(
            ApiException.class, () -> userService.changePassword(1L, passwordChangeRequestDto));

    assertThat(thrown.getStatus()).isEqualTo(VALIDATION_ERROR);
  }

  @Test
  public void sendPasswordRecoveryMail_success() {
    User dbUser = User.builder().email("fake@gmail.com").build();
    when(userRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.of(dbUser));

    userService.sendPasswordRecoveryMail(new PasswordRecoveryEmailDto("fake@gmail.com"));

    verify(emailService, times(1)).sendPasswordRecoveryMail(eq("fake@gmail.com"), any(), any());
    assertThat(dbUser.getResetToken()).isNotEmpty();
  }

  @Test
  public void sendPasswordRecoveryMail_emailDoesNotExist_throwsException() {
    when(userRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.empty());

    ApiException thrown =
        assertThrows(
            ApiException.class,
            () ->
                userService.sendPasswordRecoveryMail(
                    new PasswordRecoveryEmailDto("fake@gmail.com")));

    assertThat(thrown.getStatus()).isEqualTo(VALIDATION_ERROR);
  }

  @Test
  public void resetPassword_success_changesPasswordAndUnblocksAccount() {
    String resetToken = "token";
    User dbUser =
        User.builder()
            .id(1L)
            .resetToken(resetToken)
            .password(PASSWORD_HASH)
            .incorrectLoginAttempts(20)
            .isBlocked(true)
            .build();
    when(userRepository.findByResetToken(any())).thenReturn(Optional.of(dbUser));

    userService.resetPassword(
        PasswordResetDto.builder().newPassword("newPass").token(resetToken).build());

    assertThat(dbUser.getPassword()).isNotEqualTo(PASSWORD_HASH);
    assertThat(dbUser.getPassword()).startsWith(BCRYPT_ENCODING_PREFIX);
    assertThat(dbUser.isBlocked()).isFalse();
    assertThat(dbUser.getIncorrectLoginAttempts()).isEqualTo(0);
    assertThat(dbUser.getResetToken()).isNull();
  }

  @Test
  public void resetPassword_invalidResetToken_throwsException() {
    when(userRepository.findByResetToken(any())).thenReturn(Optional.empty());

    ApiException thrown =
        assertThrows(ApiException.class, () -> userService.resetPassword(new PasswordResetDto()));

    assertThat(thrown.getStatus()).isEqualTo(VALIDATION_ERROR);
  }
}

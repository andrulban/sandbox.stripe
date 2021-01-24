package com.andrulban.sandbox.stripe.service.impl;

import com.andrulban.sandbox.stripe.dto.*;
import com.andrulban.sandbox.stripe.entity.User;
import com.andrulban.sandbox.stripe.entity.User.UserRole;
import com.andrulban.sandbox.stripe.exception.ApiException;
import com.andrulban.sandbox.stripe.exception.ExceptionType;
import com.andrulban.sandbox.stripe.mapper.UserMapper;
import com.andrulban.sandbox.stripe.repository.UserRepository;
import com.andrulban.sandbox.stripe.service.EmailService;
import com.andrulban.sandbox.stripe.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;
  private final EmailService emailService;

  public UserServiceImpl(
      PasswordEncoder passwordEncoder, UserRepository userRepository, EmailService emailService) {
    this.passwordEncoder = passwordEncoder;
    this.userRepository = userRepository;
    this.emailService = emailService;
  }

  public Long registerUser(RegistrationDto registrationDto) {
    userRepository
        .findByEmailIgnoreCase(registrationDto.getEmail())
        .ifPresent(
            u -> {
              throw new ApiException(
                  "User with email: " + registrationDto.getEmail() + " already exists!",
                  ExceptionType.VALIDATION_ERROR);
            });

    User user = UserMapper.mapToJpa(registrationDto);
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    user.setUserRole(UserRole.CUSTOMER);
    userRepository.save(user);

    return user.getId();
  }

  public UserDto getUserById(Long id) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ApiException(
                        "User with id: " + id + " does not exist!", ExceptionType.NO_RESULT));

    return UserMapper.mapToDto(user);
  }

  public void editUserData(Long userId, UserDataEditionDto userDataEditionDto) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () ->
                    new ApiException(
                        "User with id: " + userId + " does not exist!", ExceptionType.NO_RESULT));

    userRepository
        .findByEmailIgnoreCase(userDataEditionDto.getEmail())
        .filter(u -> !u.getId().equals(userId))
        .ifPresent(
            u -> {
              throw new ApiException(
                  "User with email: " + userDataEditionDto.getEmail() + " already exists!",
                  ExceptionType.VALIDATION_ERROR);
            });

    user.setEmail(userDataEditionDto.getEmail());
    user.setFirstName(userDataEditionDto.getFirstName());
    user.setLastName(userDataEditionDto.getLastName());
    user.setPhoneNumber(userDataEditionDto.getPhoneNumber());

    userRepository.save(user);
  }

  public void changePassword(Long userId, PasswordChangeRequestDto passwordChangeRequestDto) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () ->
                    new ApiException(
                        "User with id: " + userId + " does not exist!", ExceptionType.NO_RESULT));

    if (!passwordEncoder.matches(passwordChangeRequestDto.getOldPassword(), user.getPassword())) {
      throw new ApiException("Wrong password!", ExceptionType.VALIDATION_ERROR);
    }

    user.setPassword(passwordEncoder.encode(passwordChangeRequestDto.getNewPassword()));
    userRepository.saveAndFlush(user);
  }

  public void sendPasswordRecoveryMail(PasswordRecoveryEmailDto recoveryEmailDto) {
    User user =
        userRepository
            .findByEmailIgnoreCase(recoveryEmailDto.getEmail())
            .orElseThrow(
                () ->
                    new ApiException(
                        "User with email: " + recoveryEmailDto.getEmail() + " does not exist!",
                        ExceptionType.VALIDATION_ERROR));

    user.setResetToken(UUID.randomUUID().toString());
    userRepository.save(user);

    emailService.sendPasswordRecoveryMail(
        user.getEmail(), "Rent password reset", user.getResetToken());
  }

  public void resetPassword(PasswordResetDto resetDto) {
    User user =
        userRepository
            .findByResetToken(resetDto.getToken())
            .orElseThrow(
                () -> new ApiException("Invalid reset token!", ExceptionType.VALIDATION_ERROR));

    user.setPassword(passwordEncoder.encode(resetDto.getNewPassword()));
    user.setIncorrectLoginAttempts(0);
    user.setBlocked(false);
    user.setResetToken(null);

    userRepository.save(user);
  }
}

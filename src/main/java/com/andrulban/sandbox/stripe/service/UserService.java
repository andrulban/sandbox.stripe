package com.andrulban.sandbox.stripe.service;

import com.andrulban.sandbox.stripe.dto.*;

public interface UserService {

  Long registerUser(RegistrationDto registrationDto);

  UserDto getUserById(Long id);

  void editUserData(Long userId, UserDataEditionDto userDataEditionDto);

  void changePassword(Long userId, PasswordChangeRequestDto passwordChangeRequestDto);

  void sendPasswordRecoveryMail(PasswordRecoveryEmailDto recoveryEmailDto);

  void resetPassword(PasswordResetDto resetDto);
}

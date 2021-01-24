package com.andrulban.sandbox.stripe.controller;

import com.andrulban.sandbox.stripe.dto.*;
import com.andrulban.sandbox.stripe.service.UserService;
import com.andrulban.sandbox.stripe.utils.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/users")
public class UserRestController {

  private final UserService userService;

  public UserRestController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping(value = "/registration", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity registerUser(@Valid @RequestBody RegistrationDto registrationDto) {
    Long id = userService.registerUser(registrationDto);
    return ResponseEntity.created(URI.create("/users/" + id)).build();
  }

  @PreAuthorize("hasAnyRole('CUSTOMER')")
  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<UserDto> getUserById(@PathVariable("id") Long id) {
    return ResponseEntity.ok(userService.getUserById(id));
  }

  @PreAuthorize("hasAnyRole('CUSTOMER')")
  @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity editUserData(
      @Valid @RequestBody UserDataEditionDto userDataEditionDto, Authentication authentication) {
    CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
    userService.editUserData(customUserDetails.getId(), userDataEditionDto);
    return ResponseEntity.created(URI.create("/users/" + customUserDetails.getId())).build();
  }

  @PreAuthorize("hasAnyRole('CUSTOMER')")
  @PutMapping(value = "/password-change", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity changePassword(
      @Valid @RequestBody PasswordChangeRequestDto passwordChangeRequestDto,
      Authentication authentication) {
    CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
    userService.changePassword(customUserDetails.getId(), passwordChangeRequestDto);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/password-recovery-mail")
  public ResponseEntity sendPasswordRecoveryMail(
      @Valid @RequestBody PasswordRecoveryEmailDto passwordRecoveryEmailDto) {
    userService.sendPasswordRecoveryMail(passwordRecoveryEmailDto);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PutMapping("/password-reset")
  public ResponseEntity resetPassword(@Valid @RequestBody PasswordResetDto passwordResetDto) {
    userService.resetPassword(passwordResetDto);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
}

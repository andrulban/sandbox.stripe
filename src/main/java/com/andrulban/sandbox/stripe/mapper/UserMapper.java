package com.andrulban.sandbox.stripe.mapper;

import com.andrulban.sandbox.stripe.dto.RegistrationDto;
import com.andrulban.sandbox.stripe.dto.UserDto;
import com.andrulban.sandbox.stripe.entity.User;

// TODO: use MapStruct instead of manual mapper
public class UserMapper {

  public static UserDto mapToDto(User user) {
    return UserDto.builder()
        .id(user.getId())
        .email(user.getEmail())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .userRole(user.getUserRole())
        .phoneNumber(user.getPhoneNumber())
        .build();
  }

  public static User mapToJpa(RegistrationDto registrationDto) {
    return User.builder()
        .email(registrationDto.getEmail())
        .firstName(registrationDto.getFirstName())
        .lastName(registrationDto.getLastName())
        .phoneNumber(registrationDto.getPhoneNumber())
        .password(registrationDto.getPassword())
        .build();
  }
}

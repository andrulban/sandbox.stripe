package com.andrulban.sandbox.stripe.dto;

import com.andrulban.sandbox.stripe.entity.User.UserRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {

  private Long id;
  private String email;
  private String firstName;
  private String lastName;
  private String phoneNumber;
  private UserRole userRole;
}

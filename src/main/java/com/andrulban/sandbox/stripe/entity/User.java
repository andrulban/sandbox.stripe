package com.andrulban.sandbox.stripe.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(name = "UK_email", columnNames = "email")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends TimestampEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false, unique = true)
  private Long id;

  @Column(name = "email", nullable = false, unique = true, length = 100)
  private String email;

  @Column(nullable = false, length = 50)
  private String firstName;

  @Column(nullable = false, length = 50)
  private String lastName;

  @Column(nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private UserRole userRole;

  @Column(nullable = false, length = 100)
  private String password;

  @Column(length = 36)
  private String resetToken;

  @Column(length = 10, nullable = false)
  private String phoneNumber;

  @Column(nullable = false)
  private int incorrectLoginAttempts;

  @Column(nullable = false)
  private boolean isBlocked;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
  private Set<Transaction> transactions;

  public enum UserRole {
    CUSTOMER;
  }
}

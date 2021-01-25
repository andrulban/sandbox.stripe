package com.andrulban.sandbox.stripe.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "payment_transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction extends TimestampEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false, unique = true)
  private long id;

  @Column(nullable = false, length = 500)
  private String description;

  @Column(nullable = false)
  private Long amount;

  @Column(nullable = false, length = 5)
  @Enumerated(EnumType.STRING)
  private Currency currency;

  @Column(nullable = false, unique = true)
  private String stripeToken;

  @Column(nullable = false)
  private String stripeEmail;

  @Column(unique = true)
  private String stripeId;

  @Column private String stripeStatus;

  @Column private Long fee;

  @Column(nullable = false, length = 40)
  @Enumerated(EnumType.STRING)
  private Status status;

  @Column(columnDefinition = "text")
  private String errorMessage;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
  private User user;

  @Column(name = "user_id", nullable = false, insertable = false, updatable = false)
  private Long userId;

  public enum Currency {
    EUR,
    USD;
  }

  public enum Status {
    NEW,
    ERROR,
    SUCCESS;
  }
}

package com.andrulban.sandbox.stripe.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

// TODO create predefined annotation containing most frequently used Lombok annotations
@Getter
@Setter
@MappedSuperclass
public abstract class TimestampEntity {

  @Column(nullable = false)
  @Temporal(TemporalType.DATE)
  protected Date creationDate;

  @Column(nullable = false)
  @Temporal(TemporalType.DATE)
  protected Date updateDate;

  @PrePersist
  protected void prePersist() {
    if (creationDate == null) {
      creationDate = new Date();
    }
    if (updateDate == null) {
      updateDate = new Date();
    }
  }

  @PreUpdate
  private void preUpdate() {
    updateDate = new Date();
  }
}

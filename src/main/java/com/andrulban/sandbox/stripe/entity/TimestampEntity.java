package com.andrulban.sandbox.stripe.entity;

import javax.persistence.*;
import java.util.Date;

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
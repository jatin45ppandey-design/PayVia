package com.payvia.entity;

public enum PaymentIntentStatus {
    CREATED,
    SIGNED,
    QUEUED,
    RELAYING,
    INVALID,
    EXPIRED,
    SETTLED,
    FAILED
}
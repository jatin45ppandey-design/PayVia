package com.payvia.entity;

public enum TransactionStatus {
    CREATED,
    QUEUED,
    RELAYING,
    BRIDGE_REACHED,
    UPLOADED,
    PROCESSING,
    FAILED_RETRYABLE,
    SETTLED,
    REJECTED,
    EXPIRED,
    FAILED
}

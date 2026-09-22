package com.payvia.entity;

public enum RelayPacketState {
    CREATED,
    QUEUED,
    RELAYING,
    BRIDGE_REACHED,
    UPLOADED,
    EXPIRED,
    DROPPED
}

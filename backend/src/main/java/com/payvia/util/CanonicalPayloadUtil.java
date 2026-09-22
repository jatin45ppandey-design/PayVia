package com.payvia.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class CanonicalPayloadUtil {

    public static String buildCanonicalPayload(
            String version,
            UUID transactionId,
            UUID senderUserId,
            UUID senderDeviceId,
            UUID receiverUserId,
            BigDecimal amount,
            String currency,
            UUID nonce,
            Long sequenceNumber,
            OffsetDateTime createdAt,
            OffsetDateTime expiresAt) {

        return String.join("|",
                version,
                transactionId.toString().toLowerCase(),
                senderUserId.toString().toLowerCase(),
                senderDeviceId.toString().toLowerCase(),
                receiverUserId.toString().toLowerCase(),
                amount.setScale(2, RoundingMode.HALF_UP).toString(),
                currency.toUpperCase(),
                nonce.toString().toLowerCase(),
                sequenceNumber.toString(),
                createdAt.format(DateTimeFormatter.ISO_INSTANT),
                expiresAt.format(DateTimeFormatter.ISO_INSTANT)
        );
    }
}

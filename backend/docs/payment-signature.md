# Offline Payment Signature Format

The canonical payload for offline payment intents is a strict pipe-separated (|) string.
It must be signed using ECDSA P-256 with SHA-256.

## Format
version|transactionId|senderUserId|senderDeviceId|receiverUserId|amount|currency|nonce|sequenceNumber|createdAt|expiresAt

## Requirements
- version: literal "1"
- transactionId: UUID string (lowercase)
- senderUserId: UUID string (lowercase)
- senderDeviceId: UUID string (lowercase)
- receiverUserId: UUID string (lowercase)
- amount: Exact 2 decimal places, no grouping, e.g., "500.00"
- currency: literal "INR"
- nonce: UUID string (lowercase)
- sequenceNumber: Integer string, e.g., "1"
- createdAt: ISO-8601 UTC with 'Z', e.g., "2024-01-01T12:00:00Z"
- expiresAt: ISO-8601 UTC with 'Z', e.g., "2024-01-02T12:00:00Z"
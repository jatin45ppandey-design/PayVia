export function buildCanonicalPayload(
  version: string,
  transactionId: string,
  senderUserId: string,
  senderDeviceId: string,
  receiverUserId: string,
  amount: string,
  currency: string,
  nonce: string,
  sequenceNumber: string,
  createdAt: string,
  expiresAt: string
): string {
  // Canonical format: version|transactionId|senderUserId|senderDeviceId|receiverUserId|amount|currency|nonce|sequenceNumber|createdAt|expiresAt
  return [
    version,
    transactionId.toLowerCase(),
    senderUserId.toLowerCase(),
    senderDeviceId.toLowerCase(),
    receiverUserId.toLowerCase(),
    amount,
    currency.toUpperCase(),
    nonce.toLowerCase(),
    sequenceNumber,
    createdAt,
    expiresAt
  ].join('|');
}

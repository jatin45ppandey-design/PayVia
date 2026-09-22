-- Add pending_offline_outgoing to wallets
ALTER TABLE wallets ADD COLUMN pending_offline_outgoing DECIMAL(19,2) NOT NULL DEFAULT 0.00;

-- Backfill pending_offline_outgoing for existing QUEUED, RELAYING, BRIDGE_REACHED, UPLOADED, PROCESSING transactions
-- Since we deduct from offline_reserved_balance at QUEUED state, we need to balance it correctly
UPDATE wallets w
SET pending_offline_outgoing = COALESCE(
    (SELECT SUM(t.amount)
     FROM transactions t
     WHERE t.sender_id = w.user_id
       AND t.mode = 'OFFLINE_RELAY'
       AND t.status IN ('QUEUED', 'RELAYING', 'BRIDGE_REACHED', 'UPLOADED', 'PROCESSING')), 0.00
);

-- Create settlement_attempts table
CREATE TABLE settlement_attempts (
    id UUID PRIMARY KEY,
    transaction_id UUID NOT NULL,
    packet_id UUID NOT NULL,
    bridge_node_id UUID NOT NULL,
    attempt_number INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    failure_code VARCHAR(100),
    failure_message TEXT,
    received_at TIMESTAMP WITH TIME ZONE NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    retryable BOOLEAN NOT NULL DEFAULT false,
    CONSTRAINT fk_settlement_attempt_transaction FOREIGN KEY (transaction_id) REFERENCES transactions (id),
    CONSTRAINT fk_settlement_attempt_packet FOREIGN KEY (packet_id) REFERENCES relay_packets (id),
    CONSTRAINT fk_settlement_attempt_bridge FOREIGN KEY (bridge_node_id) REFERENCES relay_nodes (id)
);

-- Create settlements table
CREATE TABLE settlements (
    id UUID PRIMARY KEY,
    transaction_id UUID NOT NULL UNIQUE,
    sender_user_id UUID NOT NULL,
    receiver_user_id UUID NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    settled_at TIMESTAMP WITH TIME ZONE NOT NULL,
    settlement_reference VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    CONSTRAINT fk_settlement_transaction FOREIGN KEY (transaction_id) REFERENCES transactions (id),
    CONSTRAINT fk_settlement_sender FOREIGN KEY (sender_user_id) REFERENCES users (id),
    CONSTRAINT fk_settlement_receiver FOREIGN KEY (receiver_user_id) REFERENCES users (id)
);

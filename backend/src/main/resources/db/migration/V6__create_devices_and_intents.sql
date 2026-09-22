CREATE TABLE devices (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    device_name VARCHAR(100) NOT NULL,
    device_type VARCHAR(20) NOT NULL,
    public_key TEXT NOT NULL,
    key_algorithm VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    registered_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_seen_at TIMESTAMP WITH TIME ZONE,
    last_sequence_number BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_devices_user_id FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_devices_user_id ON devices(user_id);

CREATE TABLE offline_payment_intents (
    id UUID PRIMARY KEY,
    transaction_id UUID NOT NULL UNIQUE,
    sender_user_id UUID NOT NULL,
    sender_device_id UUID NOT NULL,
    receiver_user_id UUID NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    nonce UUID NOT NULL UNIQUE,
    sequence_number BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    signature TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_offline_payment_intents_transaction_id FOREIGN KEY (transaction_id) REFERENCES transactions (id),
    CONSTRAINT fk_offline_payment_intents_sender_user_id FOREIGN KEY (sender_user_id) REFERENCES users (id),
    CONSTRAINT fk_offline_payment_intents_sender_device_id FOREIGN KEY (sender_device_id) REFERENCES devices (id),
    CONSTRAINT fk_offline_payment_intents_receiver_user_id FOREIGN KEY (receiver_user_id) REFERENCES users (id),
    CONSTRAINT uq_device_sequence UNIQUE (sender_device_id, sequence_number)
);

CREATE INDEX idx_offline_payment_intents_sender_id ON offline_payment_intents(sender_user_id);
CREATE INDEX idx_offline_payment_intents_receiver_id ON offline_payment_intents(receiver_user_id);
CREATE INDEX idx_offline_payment_intents_status ON offline_payment_intents(status);

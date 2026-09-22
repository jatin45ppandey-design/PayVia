CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    public_reference VARCHAR(50) NOT NULL UNIQUE,
    sender_id UUID NOT NULL,
    receiver_id UUID NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    mode VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    failure_reason VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    settled_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_transactions_sender_id FOREIGN KEY (sender_id) REFERENCES users (id),
    CONSTRAINT fk_transactions_receiver_id FOREIGN KEY (receiver_id) REFERENCES users (id)
);

CREATE INDEX idx_transactions_sender_id ON transactions(sender_id);
CREATE INDEX idx_transactions_receiver_id ON transactions(receiver_id);
CREATE INDEX idx_transactions_public_reference ON transactions(public_reference);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);

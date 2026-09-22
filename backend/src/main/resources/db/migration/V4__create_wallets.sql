CREATE TABLE wallets (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    available_balance DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    offline_reserved_balance DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_wallets_user_id FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_wallets_user_id ON wallets(user_id);

CREATE TABLE app_health (
    id BIGSERIAL PRIMARY KEY,
    message VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO app_health (message) VALUES ('Payvia database is connected and ready.');

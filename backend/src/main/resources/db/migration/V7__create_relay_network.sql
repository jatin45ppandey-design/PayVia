CREATE TABLE relay_nodes (
    id UUID PRIMARY KEY,
    node_name VARCHAR(255) NOT NULL,
    node_type VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    has_internet BOOLEAN NOT NULL DEFAULT false,
    x_position INTEGER NOT NULL DEFAULT 0,
    y_position INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE relay_edges (
    id UUID PRIMARY KEY,
    source_node_id UUID NOT NULL REFERENCES relay_nodes(id) ON DELETE CASCADE,
    target_node_id UUID NOT NULL REFERENCES relay_nodes(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE(source_node_id, target_node_id)
);

CREATE TABLE relay_packets (
    id UUID PRIMARY KEY,
    packet_id UUID NOT NULL UNIQUE,
    transaction_id UUID NOT NULL REFERENCES transactions(id),
    offline_intent_id UUID NOT NULL REFERENCES offline_payment_intents(id),
    protocol_version VARCHAR(50) NOT NULL,
    sender_device_id UUID NOT NULL REFERENCES devices(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    hop_limit INTEGER NOT NULL,
    current_hop_count INTEGER NOT NULL DEFAULT 0,
    encrypted_payload TEXT NOT NULL,
    payload_hash VARCHAR(255) NOT NULL,
    packet_aes_key VARCHAR(255) NOT NULL,
    state VARCHAR(50) NOT NULL,
    created_by_node_id UUID REFERENCES relay_nodes(id),
    uploaded_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE node_packet_store (
    id UUID PRIMARY KEY,
    node_id UUID NOT NULL REFERENCES relay_nodes(id) ON DELETE CASCADE,
    relay_packet_id UUID NOT NULL REFERENCES relay_packets(id) ON DELETE CASCADE,
    received_at TIMESTAMP WITH TIME ZONE NOT NULL,
    forwarded_at TIMESTAMP WITH TIME ZONE,
    state VARCHAR(50) NOT NULL,
    hop_count INTEGER NOT NULL,
    previous_node_id UUID REFERENCES relay_nodes(id),
    UNIQUE(node_id, relay_packet_id)
);

CREATE TABLE relay_events (
    id UUID PRIMARY KEY,
    packet_id UUID NOT NULL REFERENCES relay_packets(id) ON DELETE CASCADE,
    from_node_id UUID REFERENCES relay_nodes(id),
    to_node_id UUID REFERENCES relay_nodes(id),
    event_type VARCHAR(50) NOT NULL,
    hop_number INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

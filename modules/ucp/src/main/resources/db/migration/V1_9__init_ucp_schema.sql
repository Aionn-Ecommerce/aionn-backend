-- UCP MODULE - COMPLETE INIT SCHEMA

CREATE TABLE agent_registries (
    agent_id VARCHAR(50) PRIMARY KEY,
    owner_id VARCHAR(50) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE'
);

CREATE TABLE agent_sessions (
    session_id VARCHAR(50) PRIMARY KEY,
    agent_id VARCHAR(50) NOT NULL,
    context_data JSON,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP,
    FOREIGN KEY (agent_id) REFERENCES agent_registries(agent_id)
);

CREATE TABLE ucp_transactions (
    transaction_id VARCHAR(50) PRIMARY KEY,
    session_id VARCHAR(50) NOT NULL,
    raw_input TEXT,
    intent_data JSON,
    proposal_data JSON,
    status VARCHAR(20) DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES agent_sessions(session_id)
);

CREATE TABLE agent_quotas (
    agent_id VARCHAR(50) PRIMARY KEY,
    daily_limit DECIMAL(15,2),
    monthly_limit DECIMAL(15,2),
    current_usage DECIMAL(15,2) DEFAULT 0,
    currency VARCHAR(10) DEFAULT 'VND',
    FOREIGN KEY (agent_id) REFERENCES agent_registries(agent_id)
);

CREATE TABLE ucp_configurations (
    version VARCHAR(20) PRIMARY KEY,
    mapping_rules JSON,
    release_notes TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    effective_at TIMESTAMP
);

CREATE TABLE account_deletion_requests (
    deletion_request_id VARCHAR(26) PRIMARY KEY,
    user_id VARCHAR(26) NOT NULL,
    status VARCHAR(20) NOT NULL,
    requested_at TIMESTAMP NOT NULL,
    scheduled_deletion_at TIMESTAMP NOT NULL,
    canceled_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE INDEX idx_account_deletion_user_id ON account_deletion_requests(user_id);
CREATE INDEX idx_account_deletion_status ON account_deletion_requests(status);

CREATE TABLE data_export_requests (
    export_request_id VARCHAR(26) PRIMARY KEY,
    user_id VARCHAR(26) NOT NULL,
    status VARCHAR(20) NOT NULL,
    requested_at TIMESTAMP NOT NULL,
    file_url TEXT,
    completed_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE INDEX idx_data_export_user_id ON data_export_requests(user_id);
CREATE INDEX idx_data_export_status ON data_export_requests(status);

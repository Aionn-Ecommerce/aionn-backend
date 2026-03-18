CREATE TABLE IF NOT EXISTS user_roles (
    user_id VARCHAR(26) NOT NULL,
    role VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles(user_id);

INSERT INTO user_roles (user_id, role)
SELECT user_id, role
FROM users
WHERE role IS NOT NULL
ON CONFLICT DO NOTHING;

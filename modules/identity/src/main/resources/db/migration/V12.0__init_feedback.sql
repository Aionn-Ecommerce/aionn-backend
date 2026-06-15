-- User feedback table. Captures both authenticated and anonymous submissions
-- (the contact-us / feedback dialogs in the storefront header).
CREATE TABLE user_feedbacks (
    feedback_id  VARCHAR(50) PRIMARY KEY,
    user_id      VARCHAR(50),
    category     VARCHAR(30) NOT NULL,
    subject      VARCHAR(200),
    content      TEXT NOT NULL,
    rating       SMALLINT,
    contact_email VARCHAR(150),
    contact_phone VARCHAR(30),
    status       VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    handled_by   VARCHAR(50),
    handled_at   TIMESTAMPTZ,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_user_feedbacks_user_id ON user_feedbacks(user_id);
CREATE INDEX idx_user_feedbacks_status ON user_feedbacks(status);
CREATE INDEX idx_user_feedbacks_created_at ON user_feedbacks(created_at DESC);

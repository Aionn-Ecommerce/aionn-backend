-- Add optimistic-lock counters required by JPA @Version on UserEntity,
-- AuthSessionEntity and KycProfileEntity. Without these columns Hibernate fails at
-- startup with SchemaManagementException because the entity declares a non-nullable
-- field that does not exist in the schema.
--
-- Default 0 is applied so existing rows remain valid; future updates will increment
-- the counter automatically and detect lost-update races.

ALTER TABLE users
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE auth_sessions
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE kyc_profiles
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

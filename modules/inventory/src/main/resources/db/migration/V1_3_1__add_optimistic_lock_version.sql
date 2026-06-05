ALTER TABLE stock_reservations
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE stock_transfers
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

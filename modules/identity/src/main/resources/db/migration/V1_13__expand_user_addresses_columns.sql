ALTER TABLE user_addresses
    ADD COLUMN contact_name VARCHAR(100),
    ADD COLUMN province_code VARCHAR(20),
    ADD COLUMN province_name VARCHAR(255),
    ADD COLUMN district_code VARCHAR(20),
    ADD COLUMN district_name VARCHAR(255),
    ADD COLUMN ward_code VARCHAR(20),
    ADD COLUMN ward_name VARCHAR(255),
    ADD COLUMN detail_address VARCHAR(500),
    ADD COLUMN address_type VARCHAR(20);

UPDATE user_addresses
SET contact_name = COALESCE(contact_name, ''),
    province_code = COALESCE(province_code, ''),
    district_code = COALESCE(district_code, ''),
    ward_code = COALESCE(ward_code, ''),
    detail_address = COALESCE(detail_address, full_address),
    address_type = COALESCE(address_type, 'HOME');

ALTER TABLE user_addresses
    ALTER COLUMN contact_name SET NOT NULL,
    ALTER COLUMN province_code SET NOT NULL,
    ALTER COLUMN district_code SET NOT NULL,
    ALTER COLUMN ward_code SET NOT NULL,
    ALTER COLUMN detail_address SET NOT NULL,
    ALTER COLUMN address_type SET NOT NULL;

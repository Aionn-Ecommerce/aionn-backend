-- Geography tables for address reference data

-- Countries table
CREATE TABLE countries (
    code VARCHAR(2) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    name_en VARCHAR(100),
    phone_code VARCHAR(10),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_country_name UNIQUE (name)
);

-- Provinces/States table
CREATE TABLE provinces (
    code VARCHAR(10) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    name_en VARCHAR(100),
    country_code VARCHAR(2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_province_country FOREIGN KEY (country_code) REFERENCES countries(code)
);

CREATE INDEX idx_province_country ON provinces(country_code);

-- Districts table
CREATE TABLE districts (
    code VARCHAR(10) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    name_en VARCHAR(100),
    province_code VARCHAR(10) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_district_province FOREIGN KEY (province_code) REFERENCES provinces(code)
);

CREATE INDEX idx_district_province ON districts(province_code);

-- Wards table
CREATE TABLE wards (
    code VARCHAR(15) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    name_en VARCHAR(100),
    district_code VARCHAR(10) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_ward_district FOREIGN KEY (district_code) REFERENCES districts(code)
);

CREATE INDEX idx_ward_district ON wards(district_code);

-- Insert Vietnam as default country
INSERT INTO countries (code, name, name_en, phone_code, active) VALUES
('VN', 'Việt Nam', 'Vietnam', '+84', TRUE);

-- Insert some major provinces in Vietnam
INSERT INTO provinces (code, name, name_en, country_code, active) VALUES
('VN-HN', 'Hà Nội', 'Hanoi', 'VN', TRUE),
('VN-SG', 'TP. Hồ Chí Minh', 'Ho Chi Minh City', 'VN', TRUE),
('VN-DN', 'Đà Nẵng', 'Da Nang', 'VN', TRUE),
('VN-HP', 'Hải Phòng', 'Hai Phong', 'VN', TRUE),
('VN-CT', 'Cần Thơ', 'Can Tho', 'VN', TRUE);

-- Insert some districts for Hanoi
INSERT INTO districts (code, name, name_en, province_code, active) VALUES
('VN-HN-BA', 'Ba Đình', 'Ba Dinh', 'VN-HN', TRUE),
('VN-HN-HK', 'Hoàn Kiếm', 'Hoan Kiem', 'VN-HN', TRUE),
('VN-HN-TX', 'Tây Hồ', 'Tay Ho', 'VN-HN', TRUE),
('VN-HN-LB', 'Long Biên', 'Long Bien', 'VN-HN', TRUE),
('VN-HN-CG', 'Cầu Giấy', 'Cau Giay', 'VN-HN', TRUE),
('VN-HN-DD', 'Đống Đa', 'Dong Da', 'VN-HN', TRUE),
('VN-HN-HBT', 'Hai Bà Trưng', 'Hai Ba Trung', 'VN-HN', TRUE),
('VN-HN-HM', 'Hoàng Mai', 'Hoang Mai', 'VN-HN', TRUE),
('VN-HN-TL', 'Thanh Xuân', 'Thanh Xuan', 'VN-HN', TRUE);

-- Insert some districts for Ho Chi Minh City
INSERT INTO districts (code, name, name_en, province_code, active) VALUES
('VN-SG-Q1', 'Quận 1', 'District 1', 'VN-SG', TRUE),
('VN-SG-Q2', 'Quận 2', 'District 2', 'VN-SG', TRUE),
('VN-SG-Q3', 'Quận 3', 'District 3', 'VN-SG', TRUE),
('VN-SG-Q4', 'Quận 4', 'District 4', 'VN-SG', TRUE),
('VN-SG-Q5', 'Quận 5', 'District 5', 'VN-SG', TRUE),
('VN-SG-Q6', 'Quận 6', 'District 6', 'VN-SG', TRUE),
('VN-SG-Q7', 'Quận 7', 'District 7', 'VN-SG', TRUE),
('VN-SG-Q8', 'Quận 8', 'District 8', 'VN-SG', TRUE),
('VN-SG-Q9', 'Quận 9', 'District 9', 'VN-SG', TRUE),
('VN-SG-Q10', 'Quận 10', 'District 10', 'VN-SG', TRUE),
('VN-SG-Q11', 'Quận 11', 'District 11', 'VN-SG', TRUE),
('VN-SG-Q12', 'Quận 12', 'District 12', 'VN-SG', TRUE),
('VN-SG-TD', 'Thủ Đức', 'Thu Duc', 'VN-SG', TRUE),
('VN-SG-BT', 'Bình Thạnh', 'Binh Thanh', 'VN-SG', TRUE),
('VN-SG-PN', 'Phú Nhuận', 'Phu Nhuan', 'VN-SG', TRUE),
('VN-SG-TB', 'Tân Bình', 'Tan Binh', 'VN-SG', TRUE),
('VN-SG-TP', 'Tân Phú', 'Tan Phu', 'VN-SG', TRUE),
('VN-SG-GV', 'Gò Vấp', 'Go Vap', 'VN-SG', TRUE),
('VN-SG-BTA', 'Bình Tân', 'Binh Tan', 'VN-SG', TRUE);

-- Insert some wards for Ba Dinh district (Hanoi)
INSERT INTO wards (code, name, name_en, district_code, active) VALUES
('VN-HN-BA-PX', 'Phúc Xá', 'Phuc Xa', 'VN-HN-BA', TRUE),
('VN-HN-BA-TT', 'Trúc Bạch', 'Truc Bach', 'VN-HN-BA', TRUE),
('VN-HN-BA-VT', 'Vĩnh Phúc', 'Vinh Phuc', 'VN-HN-BA', TRUE),
('VN-HN-BA-CK', 'Cống Vị', 'Cong Vi', 'VN-HN-BA', TRUE),
('VN-HN-BA-LT', 'Liễu Giai', 'Lieu Giai', 'VN-HN-BA', TRUE),
('VN-HN-BA-NB', 'Nguyễn Trung Trực', 'Nguyen Trung Truc', 'VN-HN-BA', TRUE),
('VN-HN-BA-QT', 'Quán Thánh', 'Quan Thanh', 'VN-HN-BA', TRUE),
('VN-HN-BA-NP', 'Ngọc Hà', 'Ngoc Ha', 'VN-HN-BA', TRUE),
('VN-HN-BA-DK', 'Điện Biên', 'Dien Bien', 'VN-HN-BA', TRUE),
('VN-HN-BA-DT', 'Đội Cấn', 'Doi Can', 'VN-HN-BA', TRUE),
('VN-HN-BA-NK', 'Ngọc Khánh', 'Ngoc Khanh', 'VN-HN-BA', TRUE),
('VN-HN-BA-KM', 'Kim Mã', 'Kim Ma', 'VN-HN-BA', TRUE),
('VN-HN-BA-GV', 'Giảng Võ', 'Giang Vo', 'VN-HN-BA', TRUE),
('VN-HN-BA-TH', 'Thành Công', 'Thanh Cong', 'VN-HN-BA', TRUE);

-- Insert some wards for District 1 (Ho Chi Minh City)
INSERT INTO wards (code, name, name_en, district_code, active) VALUES
('VN-SG-Q1-BN', 'Bến Nghé', 'Ben Nghe', 'VN-SG-Q1', TRUE),
('VN-SG-Q1-BT', 'Bến Thành', 'Ben Thanh', 'VN-SG-Q1', TRUE),
('VN-SG-Q1-NT', 'Nguyễn Thái Bình', 'Nguyen Thai Binh', 'VN-SG-Q1', TRUE),
('VN-SG-Q1-PNL', 'Phạm Ngũ Lão', 'Pham Ngu Lao', 'VN-SG-Q1', TRUE),
('VN-SG-Q1-CK', 'Cầu Kho', 'Cau Kho', 'VN-SG-Q1', TRUE),
('VN-SG-Q1-OC', 'Cầu Ông Lãnh', 'Cau Ong Lanh', 'VN-SG-Q1', TRUE),
('VN-SG-Q1-DK', 'Đa Kao', 'Da Kao', 'VN-SG-Q1', TRUE),
('VN-SG-Q1-TT', 'Tân Định', 'Tan Dinh', 'VN-SG-Q1', TRUE),
('VN-SG-Q1-NK', 'Nguyễn Cư Trinh', 'Nguyen Cu Trinh', 'VN-SG-Q1', TRUE),
('VN-SG-Q1-CL', 'Cô Giang', 'Co Giang', 'VN-SG-Q1', TRUE);

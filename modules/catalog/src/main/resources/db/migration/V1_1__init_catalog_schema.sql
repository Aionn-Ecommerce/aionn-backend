-- CATALOG MODULE - COMPLETE INIT SCHEMA

CREATE TABLE merchants (
    merchant_id VARCHAR(50) PRIMARY KEY,
    owner_id VARCHAR(50) NOT NULL,
    name VARCHAR(150),
    logo_url TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE'
);

CREATE TABLE categories (
    category_id VARCHAR(50) PRIMARY KEY,
    parent_id VARCHAR(50),
    name VARCHAR(150),
    slug VARCHAR(150),
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE brands (
    brand_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(150),
    logo_url TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE'
);

CREATE TABLE attribute_templates (
    template_id VARCHAR(50) PRIMARY KEY,
    category_id VARCHAR(50),
    attributes JSON,
    is_filterable BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (category_id) REFERENCES categories(category_id)
);

CREATE TABLE products (
    product_id VARCHAR(50) PRIMARY KEY,
    merchant_id VARCHAR(50) NOT NULL,
    category_id VARCHAR(50),
    brand_id VARCHAR(50),
    name VARCHAR(255),
    image_list JSON,
    ai_description TEXT,
    tags JSON,
    collection_ids JSON,
    status VARCHAR(20) DEFAULT 'DRAFT',
    FOREIGN KEY (merchant_id) REFERENCES merchants(merchant_id),
    FOREIGN KEY (category_id) REFERENCES categories(category_id),
    FOREIGN KEY (brand_id) REFERENCES brands(brand_id)
);

CREATE TABLE product_variants (
    sku_id VARCHAR(50) PRIMARY KEY,
    product_id VARCHAR(50) NOT NULL,
    attribute_values JSON,
    price DECIMAL(15,2),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

CREATE INDEX idx_products_merchant ON products(merchant_id);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_status ON products(status);

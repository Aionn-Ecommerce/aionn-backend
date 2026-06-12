CREATE TABLE merchants (
    merchant_id VARCHAR(50) PRIMARY KEY,
    owner_id    VARCHAR(50) NOT NULL,
    name        VARCHAR(150),
    logo_url    TEXT,
    description TEXT,
    status      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    version     BIGINT      NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
-- One merchant per owner (matches MerchantService.register precondition).
CREATE UNIQUE INDEX uq_merchants_owner ON merchants(owner_id);
CREATE INDEX idx_merchants_status ON merchants(status);

CREATE TABLE categories (
    category_id VARCHAR(50) PRIMARY KEY,
    parent_id   VARCHAR(50),
    name        VARCHAR(150) NOT NULL,
    slug        VARCHAR(150) NOT NULL,
    icon_url    TEXT,
    is_active   BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMPTZ,
    CONSTRAINT fk_categories_parent FOREIGN KEY (parent_id) REFERENCES categories(category_id)
);
CREATE UNIQUE INDEX uq_categories_slug ON categories(slug);
CREATE INDEX idx_categories_parent ON categories(parent_id);
-- Case-insensitive uniqueness within a parent for non-deleted categories
CREATE UNIQUE INDEX uq_categories_parent_name_active
    ON categories(parent_id, LOWER(name))
    WHERE deleted_at IS NULL;

CREATE TABLE brands (
    brand_id    VARCHAR(50) PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    logo_url    TEXT,
    description TEXT,
    status      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
-- Case-insensitive uniqueness, soft-deleted brands keep their row.
CREATE UNIQUE INDEX uq_brands_name_active
    ON brands(LOWER(name))
    WHERE status <> 'DELETED';

CREATE TABLE attribute_templates (
    template_id VARCHAR(50) PRIMARY KEY,
    category_id VARCHAR(50) NOT NULL UNIQUE,
    attributes  JSONB       NOT NULL DEFAULT '{}'::jsonb,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_attribute_templates_category FOREIGN KEY (category_id) REFERENCES categories(category_id)
);

CREATE TABLE products (
    product_id     VARCHAR(50) PRIMARY KEY,
    merchant_id    VARCHAR(50) NOT NULL,
    brand_id       VARCHAR(50),
    name           VARCHAR(255) NOT NULL,
    category_ids   JSONB       NOT NULL DEFAULT '[]'::jsonb,
    image_list     JSONB       NOT NULL DEFAULT '[]'::jsonb,
    tags           JSONB       NOT NULL DEFAULT '[]'::jsonb,
    collection_ids JSONB       NOT NULL DEFAULT '[]'::jsonb,
    attributes     JSONB       NOT NULL DEFAULT '{}'::jsonb,
    ai_description TEXT,
    status         VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    version        BIGINT      NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_products_merchant FOREIGN KEY (merchant_id) REFERENCES merchants(merchant_id),
    CONSTRAINT fk_products_brand    FOREIGN KEY (brand_id)    REFERENCES brands(brand_id)
);
CREATE INDEX idx_products_merchant ON products(merchant_id);
CREATE INDEX idx_products_brand    ON products(brand_id);
CREATE INDEX idx_products_status   ON products(status);
-- jsonb_path_ops: queries must use the @> containment form (not ?, ?? or ?|).
CREATE INDEX idx_products_category_ids_gin ON products USING GIN (category_ids   jsonb_path_ops);
CREATE INDEX idx_products_collections_gin  ON products USING GIN (collection_ids jsonb_path_ops);
CREATE INDEX idx_products_tags_gin         ON products USING GIN (tags           jsonb_path_ops);

CREATE TABLE product_variants (
    sku_id           VARCHAR(50) PRIMARY KEY,
    product_id       VARCHAR(50) NOT NULL,
    attribute_values JSONB       NOT NULL DEFAULT '{}'::jsonb,
    price            NUMERIC(18,2),
    currency         VARCHAR(3),
    CONSTRAINT fk_variants_product FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
);
CREATE INDEX idx_variants_product ON product_variants(product_id);

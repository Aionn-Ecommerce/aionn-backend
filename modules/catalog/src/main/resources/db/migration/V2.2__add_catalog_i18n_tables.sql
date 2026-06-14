-- Create translation tables for catalog internationalization (song ngữ)

CREATE TABLE product_translations (
    product_id VARCHAR(50) NOT NULL,
    locale VARCHAR(5) NOT NULL,
    name VARCHAR(255) NOT NULL,
    ai_description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (product_id, locale),
    CONSTRAINT fk_product_translations_product FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
);
CREATE INDEX idx_product_translations_locale ON product_translations(locale);

CREATE TABLE category_translations (
    category_id VARCHAR(50) NOT NULL,
    locale VARCHAR(5) NOT NULL,
    name VARCHAR(150) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (category_id, locale),
    CONSTRAINT fk_category_translations_category FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE CASCADE
);
CREATE INDEX idx_category_translations_locale ON category_translations(locale);

CREATE TABLE brand_translations (
    brand_id VARCHAR(50) NOT NULL,
    locale VARCHAR(5) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (brand_id, locale),
    CONSTRAINT fk_brand_translations_brand FOREIGN KEY (brand_id) REFERENCES brands(brand_id) ON DELETE CASCADE
);
CREATE INDEX idx_brand_translations_locale ON brand_translations(locale);

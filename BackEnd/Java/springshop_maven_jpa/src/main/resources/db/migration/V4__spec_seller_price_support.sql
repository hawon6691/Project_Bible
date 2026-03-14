CREATE TABLE IF NOT EXISTS spec_definitions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id BIGINT NOT NULL,
    spec_key VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    input_type VARCHAR(50) NOT NULL DEFAULT 'TEXT',
    options_json TEXT,
    unit VARCHAR(50),
    sort_order INT NOT NULL DEFAULT 0,
    is_filterable BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_spec_definitions_category FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT uk_spec_definitions_category_key UNIQUE (category_id, spec_key)
);

CREATE TABLE IF NOT EXISTS price_alerts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    target_price DECIMAL(15, 2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_price_alerts_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_price_alerts_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT uk_price_alerts_user_product UNIQUE (user_id, product_id)
);

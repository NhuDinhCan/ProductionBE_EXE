CREATE TABLE IF NOT EXISTS learning_resources (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    career_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    resource_type VARCHAR(50) NOT NULL,
    level VARCHAR(50),
    url VARCHAR(1000),
    thumbnail_url VARCHAR(1000),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_learning_resources_career FOREIGN KEY (career_id) REFERENCES career(id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_saved_resources (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    resource_id BIGINT NOT NULL,
    saved_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_saved_resource UNIQUE (user_id, resource_id),
    CONSTRAINT fk_user_saved_resources_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_saved_resources_resource FOREIGN KEY (resource_id) REFERENCES learning_resources(id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE seguidores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seguidor_id BIGINT NOT NULL,
    seguido_id BIGINT NOT NULL,
    fecha_seguimiento DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_seguidor_seguido (seguidor_id, seguido_id),
    FOREIGN KEY (seguidor_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (seguido_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    INDEX idx_seguidor (seguidor_id),
    INDEX idx_seguido (seguido_id)
);
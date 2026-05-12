CREATE TABLE products (
    id          UUID         PRIMARY KEY,
    nombre      VARCHAR(255) NOT NULL,
    precio      DECIMAL(10,2) NOT NULL,
    descripcion TEXT,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

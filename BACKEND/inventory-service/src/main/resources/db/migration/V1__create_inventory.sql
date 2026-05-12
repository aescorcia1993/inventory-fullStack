CREATE TABLE inventory (
    id          UUID          PRIMARY KEY,
    producto_id UUID          UNIQUE NOT NULL,
    cantidad    INTEGER       NOT NULL DEFAULT 0,
    updated_at  TIMESTAMP     NOT NULL DEFAULT NOW(),
    CONSTRAINT cantidad_non_negative CHECK (cantidad >= 0)
);

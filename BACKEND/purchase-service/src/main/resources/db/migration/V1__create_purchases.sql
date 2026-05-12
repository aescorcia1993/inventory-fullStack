CREATE TABLE purchases (
    id              UUID            PRIMARY KEY,
    producto_id     UUID            NOT NULL,
    cantidad        INTEGER         NOT NULL,
    precio_unitario DECIMAL(10,2)   NOT NULL,
    total           DECIMAL(10,2)   NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE TABLE payments (
    id           UUID          PRIMARY KEY,
    purchase_id  UUID          NOT NULL,
    producto_id  UUID          NOT NULL,
    cantidad     INTEGER       NOT NULL,
    total        DECIMAL(10,2) NOT NULL,
    status       VARCHAR(20)   NOT NULL,
    received_at  TIMESTAMP     NOT NULL,
    processed_at TIMESTAMP
);

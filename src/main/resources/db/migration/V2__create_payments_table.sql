CREATE TABLE payments (
    id UUID PRIMARY KEY,
    amount DECIMAL(10,2),
    status VARCHAR(50),
    created_at TIMESTAMP
);
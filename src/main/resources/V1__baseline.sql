CREATE TABLE IF NOT EXISTS order (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    address VARCHAR NOT NULL,
    status VARCHAR NOT NULL,
    comment VARCHAR
    phone_number VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS client (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    address VARCHAR NOT NULL,
    status VARCHAR NOT NULL,
    comment VARCHAR
    phone_number VARCHAR(255) NOT NULL
);
CREATE TABLE users (
    id         UUID         PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(255) NOT NULL    UNIQUE,
    password   TEXT         NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL
);
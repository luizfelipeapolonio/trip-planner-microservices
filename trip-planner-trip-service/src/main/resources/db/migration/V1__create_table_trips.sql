CREATE TABLE trips (
    id           UUID         PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    destination  VARCHAR(255) NOT NULL,
    owner_email  VARCHAR(255) NOT NULL,
    owner_name   VARCHAR(255) NOT NULL,
    is_confirmed BOOLEAN      NOT NULL DEFAULT TRUE,
    starts_at    DATE         NOT NULL,
    ends_at      DATE         NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL
);
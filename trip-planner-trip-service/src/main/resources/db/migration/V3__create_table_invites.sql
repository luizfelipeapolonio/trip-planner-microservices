CREATE TABLE invites (
    code       UUID         NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    trip_id    UUID         NOT NULL,
    user_id    UUID         NOT NULL,
    username   VARCHAR(100) NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    is_valid   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_trip FOREIGN KEY (trip_id) REFERENCES trips(id) ON UPDATE CASCADE ON DELETE CASCADE
);
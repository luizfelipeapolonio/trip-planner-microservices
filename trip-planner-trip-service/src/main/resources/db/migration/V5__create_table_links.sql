CREATE TABLE links (
    id          UUID         NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    title       VARCHAR(255) NOT NULL,
    url         VARCHAR(255) NOT NULL,
    owner_email VARCHAR(255) NOT NULL,
    trip_id     UUID         NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL,

    CONSTRAINT fk_trip FOREIGN KEY (trip_id) REFERENCES trips(id) ON UPDATE CASCADE ON DELETE CASCADE
);
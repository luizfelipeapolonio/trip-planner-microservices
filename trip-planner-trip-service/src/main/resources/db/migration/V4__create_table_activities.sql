CREATE TABLE activities (
    id          UUID         NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    description VARCHAR(255) NOT NULL,
    trip_id     UUID         NOT NULL,
    owner_email VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT  CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL,

    CONSTRAINT fk_trip FOREIGN KEY (trip_id) REFERENCES trips(id) ON UPDATE CASCADE ON DELETE CASCADE
);
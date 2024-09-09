CREATE TABLE participants (
    id                UUID         PRIMARY KEY NOT NULL,
    name              VARCHAR(255) NOT NULL,
    email             VARCHAR(255) NOT NULL    UNIQUE,
    trip_id           UUID         NOT NULL,
    created_at        TIMESTAMP    NOT NULL    DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_trip FOREIGN KEY (trip_id) REFERENCES trips(id) ON UPDATE CASCADE ON DELETE CASCADE
);
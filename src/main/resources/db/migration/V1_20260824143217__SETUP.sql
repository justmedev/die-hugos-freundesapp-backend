CREATE TABLE users
(
    id                  SERIAL PRIMARY KEY,
    keycloak_id         VARCHAR(255)                        NOT NULL
        CONSTRAINT users_keycloak_id_unique
            UNIQUE,
    email               VARCHAR(254)                        NOT NULL
        CONSTRAINT users_email_unique
            UNIQUE,
    first_name          VARCHAR(128)                        NOT NULL,
    last_name           VARCHAR(128)                        NOT NULL,
    account_holder_name VARCHAR(255),
    account_iban        VARCHAR(50),
    birthdate           DATE                                NOT NULL,
    is_admin            BOOLEAN   DEFAULT FALSE             NOT NULL,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE cashpools
(
    id          SERIAL
        PRIMARY KEY,
    title       VARCHAR(255)                        NOT NULL,
    description VARCHAR(255)                        NOT NULL,
    is_opened   BOOLEAN   DEFAULT TRUE              NOT NULL,
    owner_id    INTEGER                             NOT NULL
        CONSTRAINT fk_cashpools_owner_id__id
            REFERENCES users
            ON UPDATE RESTRICT ON DELETE RESTRICT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE cashpool_members
(
    id          SERIAL
        PRIMARY KEY,
    user_id     INTEGER                             NOT NULL
        CONSTRAINT fk_cashpool_members_user_id__id
            REFERENCES users
            ON UPDATE RESTRICT ON DELETE CASCADE,
    cashpool_id INTEGER                             NOT NULL
        CONSTRAINT fk_cashpool_members_cashpool_id__id
            REFERENCES cashpools
            ON UPDATE RESTRICT ON DELETE CASCADE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE cashpool_transactions
(
    id           SERIAL
        PRIMARY KEY,
    owner_id     INTEGER                             NOT NULL
        CONSTRAINT fk_cashpool_transactions_owner_id__id
            REFERENCES users
            ON UPDATE RESTRICT ON DELETE RESTRICT,
    cashpool_id  INTEGER                             NOT NULL
        CONSTRAINT fk_cashpool_transactions_cashpool_id__id
            REFERENCES cashpools
            ON UPDATE RESTRICT ON DELETE CASCADE,
    amount_cents BIGINT                              NOT NULL,
    label        VARCHAR(255)                        NOT NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE cashpool_settlements
(
    id           SERIAL
        PRIMARY KEY,
    from_id      INTEGER                             NOT NULL
        CONSTRAINT fk_cashpool_settlements_from_id__id
            REFERENCES users
            ON UPDATE RESTRICT ON DELETE RESTRICT,
    to_id        INTEGER                             NOT NULL
        CONSTRAINT fk_cashpool_settlements_to_id__id
            REFERENCES users
            ON UPDATE RESTRICT ON DELETE RESTRICT,
    cashpool_id  INTEGER                             NOT NULL
        CONSTRAINT fk_cashpool_settlements_cashpool_id__id
            REFERENCES cashpools
            ON UPDATE RESTRICT ON DELETE CASCADE,
    amount_cents BIGINT                              NOT NULL,
    purpose      VARCHAR(140)                        NOT NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

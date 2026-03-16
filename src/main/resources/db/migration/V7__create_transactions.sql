CREATE TABLE transactions (
    transaction_id   INT            PRIMARY KEY AUTO_INCREMENT,
    group_id         INT            NOT NULL,
    from_user_id     INT            NOT NULL,
    to_user_id       INT            NOT NULL,
    amount           DECIMAL(10, 2) NOT NULL,
    transaction_date DATE           NOT NULL,
    settled          BOOLEAN        NOT NULL DEFAULT false,
    CONSTRAINT fk_txn_group     FOREIGN KEY (group_id)     REFERENCES `group`(group_id) ON DELETE CASCADE,
    CONSTRAINT fk_txn_from_user FOREIGN KEY (from_user_id) REFERENCES  users(id)        ON DELETE CASCADE,
    CONSTRAINT fk_txn_to_user   FOREIGN KEY (to_user_id)   REFERENCES  users(id)        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

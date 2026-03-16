CREATE TABLE expense_splits (
    split_id         INT            PRIMARY KEY AUTO_INCREMENT,
    expense_id       INT            NOT NULL,
    user_id          INT            NOT NULL,
    share_amount     DECIMAL(10, 2) NOT NULL,
    share_percentage FLOAT          NOT NULL,
    CONSTRAINT uq_expense_split UNIQUE (expense_id, user_id),
    CONSTRAINT fk_split_expense FOREIGN KEY (expense_id) REFERENCES expenses(expense_id) ON DELETE CASCADE,
    CONSTRAINT fk_split_user    FOREIGN KEY (user_id)    REFERENCES    users(id)         ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

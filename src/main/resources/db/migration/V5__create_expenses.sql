CREATE TABLE expenses (
    expense_id   INT            PRIMARY KEY AUTO_INCREMENT,
    group_id     INT            NOT NULL,
    payer_id     INT            NOT NULL,
    category_id  INT            NOT NULL,
    amount       DECIMAL(10, 2) NOT NULL,
    expense_date DATE           NOT NULL,
    description  VARCHAR(500)   NOT NULL,
    CONSTRAINT fk_expense_group    FOREIGN KEY (group_id)    REFERENCES `group`(group_id)       ON DELETE CASCADE,
    CONSTRAINT fk_expense_payer    FOREIGN KEY (payer_id)    REFERENCES  users(id)              ON DELETE CASCADE,
    CONSTRAINT fk_expense_category FOREIGN KEY (category_id) REFERENCES  categories(category_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

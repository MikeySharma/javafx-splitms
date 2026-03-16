CREATE TABLE categories (
    category_id   INT          PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(100) NOT NULL,
    category_type VARCHAR(50)  NOT NULL,
    icon          VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

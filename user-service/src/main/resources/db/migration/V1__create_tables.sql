CREATE TABLE IF NOT EXISTS users (
                                     id BIGSERIAL PRIMARY KEY,
                                     username VARCHAR(50) NOT NULL UNIQUE,
                                     password VARCHAR(100) NOT NULL,
                                     email VARCHAR(100) NOT NULL UNIQUE,
                                     first_name VARCHAR(50),
                                     last_name VARCHAR(50),
                                     role VARCHAR(10) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
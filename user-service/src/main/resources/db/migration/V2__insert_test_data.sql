INSERT INTO users (username, password, email, first_name, last_name, role)
VALUES
    ('admin', '$2a$10$E4rFpO7gRd5XFUMntS5V3OxhJDZ3Hr9nfIHKTD68M89od7WvoRkwu', 'admin@example.com', 'Admin', 'Super', 'ADMIN'),
    ('ivan', '$2a$10$oYD3O8iFTmhIUzV525hAceWR1LmasBQvfaR6Qfhn6QQfi6VBJX0by', 'ivan@example.com', 'Ivan', 'Petrov', 'USER'),
    ('maria', '$2a$10$AfXJJ4U6HGX9lhUxKrmNge95JfCSnY6uXHuj/gerP1r2/Ikkw24my', 'maria@example.com', 'Maria', 'Ivanova', 'USER');
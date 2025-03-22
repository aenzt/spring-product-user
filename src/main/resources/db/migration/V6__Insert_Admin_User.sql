-- Password: admin123 (hashed)
INSERT INTO users (name, username, email, password, created_at, updated_at)
VALUES ('Administrator', 'admin', 'admin@example.com',
'$2a$10$oE39aG10kB/rFu2vQeCJTu/V/v4n6DRR0f8WyXRiAYvBpmadoOBE.',
NOW(), NOW());

INSERT INTO user_roles (user_id, role_id)
VALUES (1, 2);
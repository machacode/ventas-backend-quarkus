-- =====================================================
-- V2: Datos iniciales para probar el sistema
-- =====================================================

-- Usuario admin por defecto
-- Email: admin@ventas.com
-- Password: Admin123!  (hasheada con BCrypt)
INSERT INTO usuarios (nombre, email, password, rol)
VALUES (
           'Administrador',
           'admin@ventas.com',
           '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj4tbQDzMu6.',
           'ADMIN'
       );

-- Productos de ejemplo
INSERT INTO productos (codigo, nombre, precio, stock, stock_minimo, unidad)
VALUES
    ('PROD-001', 'Laptop Dell Inspiron',  2500.00, 10, 2, 'UND'),
    ('PROD-002', 'Mouse Inalambrico',       25.00, 50, 5, 'UND'),
    ('PROD-003', 'Teclado Mecanico',        85.00, 30, 3, 'UND'),
    ('PROD-004', 'Monitor 24 pulgadas',    350.00, 15, 2, 'UND'),
    ('PROD-005', 'Audifonos Bluetooth',     60.00, 25, 5, 'UND');

-- Clientes de ejemplo
INSERT INTO clientes (nombre, email, telefono, documento)
VALUES
    ('Juan Perez',   'juan@email.com',  '999111222', '12345678'),
    ('Maria Garcia', 'maria@email.com', '999333444', '87654321'),
    ('Empresa ABC',  'abc@empresa.com', '999555666', '20123456789');
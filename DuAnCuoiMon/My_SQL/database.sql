create database du_an_cuoi_mon
character set utf8mb4
collate utf8mb4_unicode_ci;
use du_an_cuoi_mon;

SELECT id, name, price, image, available FROM products;
SELECT * FROM categories;
SELECT id, name, description FROM categories ORDER BY id;
-- kiểm tra dữ liệu 
SELECT p.id, p.name, p.price, p.image, p.available, c.name AS category_name
FROM products p
LEFT JOIN categories c ON p.category_id = c.id ORDER BY p.id;
SELECT COUNT(*) AS total_orders FROM food_orders;
SELECT DISTINCT status FROM food_orders;
-- Dem so khach hang
SELECT COUNT(*) AS total_customers FROM customers;
-- Dem tong so don hang
SELECT COUNT(*) AS total_orders FROM food_orders;
-- Dem so don theo trang thai
SELECT status, COUNT(*) AS so_luong FROM food_orders GROUP BY status;
-- Tong thanh toan da ghi nhan
SELECT COALESCE(SUM(amount), 0) AS doanh_thu FROM payments WHERE payment_status = 'PAID';
-- Kiem tra so ban
SELECT status, COUNT(*) AS so_luong FROM restaurant_tables GROUP BY status;
SELECT
    payment_status,
    COUNT(*) AS so_giao_dich,
    SUM(amount) AS tong_tien
FROM payments
GROUP BY payment_status;
SELECT
    id,
    table_name,
    status,
    capacity
FROM restaurant_tables;
SELECT
    o.id,
    c.name AS customer_name,
    o.total_price,
    o.status
FROM food_orders o
LEFT JOIN customers c
    ON o.customer_id = c.id
WHERE o.status IN ('PENDING', 'PREPARING')
ORDER BY o.order_time DESC
LIMIT 5;
SELECT
    p.name AS ten_mon,
    SUM(oi.quantity) AS da_ban,
    SUM(oi.quantity * oi.price) AS doanh_thu
FROM order_items oi
JOIN products p ON oi.product_id = p.id
JOIN food_orders o ON oi.order_id = o.id
WHERE o.status = 'COMPLETED'
GROUP BY p.id, p.name
ORDER BY da_ban DESC, doanh_thu DESC
LIMIT 5;
SELECT
    status,
    COUNT(*) AS so_luong
FROM restaurant_tables
GROUP BY status;
SELECT
    MONTH(payment_time) AS thang,
    SUM(amount) AS doanh_thu
FROM payments
WHERE payment_status = 'PAID'
  AND YEAR(payment_time) = YEAR(CURDATE())
GROUP BY MONTH(payment_time)
ORDER BY thang;
-- ======================================
-- 6. KIEM TRA DOANH THU 12 THANG
-- ======================================
SELECT
    MONTH(payment_time) AS thang,
    COUNT(*) AS so_giao_dich,
    FORMAT(SUM(amount), 0) AS doanh_thu_vnd
FROM payments
WHERE payment_status = 'PAID'
  AND YEAR(payment_time) = @report_year
GROUP BY MONTH(payment_time)
ORDER BY thang;
SELECT
    MONTH(order_time) AS thang,
    COUNT(*) AS tong_don,
    SUM(
        CASE
            WHEN status IN ('PENDING', 'PREPARING')
            THEN 1 ELSE 0
        END
    ) AS dang_xu_ly,
    SUM(
        CASE
            WHEN status = 'COMPLETED'
            THEN 1 ELSE 0
        END
    ) AS hoan_thanh
FROM food_orders WHERE YEAR(order_time) = 2026 GROUP BY MONTH(order_time) ORDER BY thang;
-- kiểm tra toàn bộ đơn hàng
SELECT *
FROM food_orders
ORDER BY order_time DESC;


-- đối chíu với emtity của category
create table categories (
	id bigint auto_increment primary key, -- id bigint là Long
    name varchar(255),
    description varchar(255)
);
-- đối chíu với bảng customers 
create table customers (
	id bigint auto_increment primary key,
    name varchar(255),
    phone varchar(255), -- nếu dùng int thì số 0 có trong dữ liệu sẽ không hiện
	address varchar(255)
);
--
create table restaurant_tables (
	id bigint auto_increment primary key,
    table_name varchar(255),
    status varchar(255),
    capacity int
);
-- đối chiếu bảng Products
create table products (
	id bigint auto_increment primary key,
    name varchar(255),
    price double,
    description varchar(255),
    image varchar(255),
    available boolean,
    category_id bigint,
    
    constraint fk_product_category
		foreign key (category_id)
        references categories(id)
);
-- đối chiếu bảng food_orders
create table food_orders (
	id bigint auto_increment primary key,
    order_type varchar(255),
    order_time datetime,
    status varchar(255),
    total_price double,
    note varchar(255),
    delivery_address varchar(255),
    
    customer_id bigint,
    table_id bigint,
    
    constraint fk_order_customer
		foreign key (customer_id)
        references customers(id),
	constraint fk_order_table
		foreign key (table_id)
        references restaurant_tables(id)
);
-- bảng order time
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quantity INT,
    price DOUBLE,

    product_id BIGINT,
    order_id BIGINT,

    CONSTRAINT fk_orderitem_product
        FOREIGN KEY (product_id)
        REFERENCES products(id),
    CONSTRAINT fk_orderitem_order
        FOREIGN KEY (order_id)
        REFERENCES food_orders(id)
);
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_method VARCHAR(255),
    payment_status VARCHAR(255),
    amount DOUBLE,
    payment_time DATETIME,

    order_id BIGINT UNIQUE,

    CONSTRAINT fk_payment_order
        FOREIGN KEY (order_id)
        REFERENCES food_orders(id)
);

-- thêm dữ liệu cho DANH MỤC
INSERT INTO categories (name, description)
VALUES
('Sushi', 'Các loại sushi và cơm cuộn'),
('Sashimi', 'Các món cá và hải sản tươi'),
('Món nóng', 'Các món chiên, cơm và mì Nhật'),
('Đồ uống', 'Các loại nước uống'),
('Combo', 'Các phần ăn kết hợp nhiều món');


USE du_an_cuoi_mon;
INSERT INTO products
(name, price, description, image, available, category_id)
VALUES
('Sushi cá hồi', 65000,
'Sushi cá hồi tươi, thơm ngon',
'/images/top12_1.jpg', 1, 1),
('Sashimi cá hồi', 99000,
'Cá hồi tươi thái lát tinh tế',
'/images/top12_2.jpg', 1, 2),
('Gà chiên giòn', 79000,
'Gà chiên vàng, giòn tan hấp dẫn',
'/images/top12_3.jpg', 1, 3),
('Mì ramen Nhật', 85000,
'Mì ramen chuẩn vị Nhật Bản',
'/images/top12_4.jpg', 1, 3),
('Cơm cuộn Maki', 55000,
'Cơm cuộn rong biển nhân đa dạng',
'/images/top12_5.jpg', 1, 1);
-- =====================================
-- 1. THEM 3 KHACH HANG DEMO
-- =====================================
INSERT INTO customers (name, phone, address)
SELECT 'Nguyễn Văn An', '0000000001', 'Địa chỉ demo 1'
WHERE NOT EXISTS (
    SELECT 1 FROM customers WHERE phone = '0000000001'
);
INSERT INTO customers (name, phone, address)
SELECT 'Trần Thị Bình', '0000000002', 'Địa chỉ demo 2'
WHERE NOT EXISTS (
    SELECT 1 FROM customers WHERE phone = '0000000002'
);
INSERT INTO customers (name, phone, address)
SELECT 'Lê Minh Châu', '0000000003', 'Địa chỉ demo 3'
WHERE NOT EXISTS (
    SELECT 1 FROM customers WHERE phone = '0000000003'
);
-- =====================================
-- 2. THEM 3 BAN AN DEMO
-- =====================================
INSERT INTO restaurant_tables
(table_name, status, capacity)
SELECT 'DEMO - Ban 01', 'OCCUPIED', 4
WHERE NOT EXISTS (
    SELECT 1 FROM restaurant_tables
    WHERE table_name = 'DEMO - Ban 01'
);
INSERT INTO restaurant_tables
(table_name, status, capacity)
SELECT 'DEMO - Ban 02', 'AVAILABLE', 4
WHERE NOT EXISTS (
    SELECT 1 FROM restaurant_tables
    WHERE table_name = 'DEMO - Ban 02'
);
INSERT INTO restaurant_tables
(table_name, status, capacity)
SELECT 'DEMO - Ban 03', 'AVAILABLE', 6
WHERE NOT EXISTS (
    SELECT 1 FROM restaurant_tables
    WHERE table_name = 'DEMO - Ban 03'
);
-- =====================================
-- 3. LAY ID KHACH HANG VA BAN AN
-- =====================================
SET @c1 = (
    SELECT id FROM customers
    WHERE phone = '0000000001' LIMIT 1
);
SET @c2 = (
    SELECT id FROM customers
    WHERE phone = '0000000002' LIMIT 1
);
SET @c3 = (
    SELECT id FROM customers
    WHERE phone = '0000000003' LIMIT 1
);
SET @t1 = (
    SELECT id FROM restaurant_tables
    WHERE table_name = 'DEMO - Ban 01' LIMIT 1
);
SET @t2 = (
    SELECT id FROM restaurant_tables
    WHERE table_name = 'DEMO - Ban 02' LIMIT 1
);
-- ===================================
-- 4. THEM 6 DON HANG DEMO
-- =====================================
-- Don 1: Cho xac nhan
INSERT INTO food_orders
(order_type, order_time, status, total_price,
 note, delivery_address, customer_id, table_id)
SELECT
'DELIVERY', '2026-10-12 09:00:00',
'PENDING', 120000,
'DEMO_DH01', 'Địa chỉ demo 1', @c1, NULL
WHERE NOT EXISTS (
    SELECT 1 FROM food_orders WHERE note = 'DEMO_DH01'
);
-- Don 2: Cho xac nhan
INSERT INTO food_orders
(order_type, order_time, status, total_price,
 note, delivery_address, customer_id, table_id)
SELECT
'DELIVERY', '2026-10-12 09:20:00',
'PENDING', 85000,
'DEMO_DH02', 'Địa chỉ demo 2', @c2, NULL
WHERE NOT EXISTS (
    SELECT 1 FROM food_orders WHERE note = 'DEMO_DH02'
);
-- Don 3: Dang chuan bi
INSERT INTO food_orders
(order_type, order_time, status, total_price,
 note, delivery_address, customer_id, table_id)
SELECT
'DINE_IN', '2026-10-12 09:40:00',
'PREPARING', 134000,
'DEMO_DH03', NULL, @c3, @t1
WHERE NOT EXISTS (
    SELECT 1 FROM food_orders WHERE note = 'DEMO_DH03'
);
-- Don 4: Da hoan thanh
INSERT INTO food_orders
(order_type, order_time, status, total_price,
 note, delivery_address, customer_id, table_id)
SELECT
'DINE_IN', '2026-10-09 12:00:00',
'COMPLETED', 164000,
'DEMO_DH04', NULL, @c1, @t2
WHERE NOT EXISTS (
    SELECT 1 FROM food_orders WHERE note = 'DEMO_DH04'
);
-- Don 5: Da hoan thanh
INSERT INTO food_orders
(order_type, order_time, status, total_price,
 note, delivery_address, customer_id, table_id)
SELECT
'DELIVERY', '2026-09-15 18:30:00',
'COMPLETED', 140000,
'DEMO_DH05', 'Địa chỉ demo 2', @c2, NULL
WHERE NOT EXISTS (
    SELECT 1 FROM food_orders WHERE note = 'DEMO_DH05'
);
-- Don 6: Da huy
INSERT INTO food_orders
(order_type, order_time, status, total_price,
 note, delivery_address, customer_id, table_id)
SELECT
'DELIVERY', '2026-08-20 11:00:00',
'CANCELLED', 79000,
'DEMO_DH06', 'Địa chỉ demo 3', @c3, NULL
WHERE NOT EXISTS (
    SELECT 1 FROM food_orders WHERE note = 'DEMO_DH06'
);
-- =====================================
-- 5. THEM CHI TIET MON AN CHO DON
-- =====================================
INSERT INTO order_items
(order_id, product_id, quantity, price)
SELECT
o.id, p.id, d.quantity, p.price
FROM (
    SELECT 'DEMO_DH01' AS ma,
           'Sushi cá hồi' AS mon, 1 AS quantity
    UNION ALL
    SELECT 'DEMO_DH01', 'Cơm cuộn Maki', 1
    UNION ALL
    SELECT 'DEMO_DH02', 'Mì ramen Nhật', 1
    UNION ALL
    SELECT 'DEMO_DH03', 'Gà chiên giòn', 1
    UNION ALL
    SELECT 'DEMO_DH03', 'Cơm cuộn Maki', 1
    UNION ALL
    SELECT 'DEMO_DH04', 'Sashimi cá hồi', 1
    UNION ALL
    SELECT 'DEMO_DH04', 'Sushi cá hồi', 1
    UNION ALL
    SELECT 'DEMO_DH05', 'Mì ramen Nhật', 1
    UNION ALL
    SELECT 'DEMO_DH05', 'Cơm cuộn Maki', 1
    UNION ALL
    SELECT 'DEMO_DH06', 'Gà chiên giòn', 1
) d
JOIN food_orders o ON o.note = d.ma
JOIN products p ON p.name = d.mon
WHERE NOT EXISTS (
    SELECT 1 FROM order_items oi
    WHERE oi.order_id = o.id
      AND oi.product_id = p.id
);
-- =====================================
-- 6. THEM THANH TOAN CHO DON HOAN THANH
-- =====================================
INSERT INTO payments
(payment_method, payment_status, amount,
 payment_time, order_id)
SELECT
    CASE
        WHEN o.note = 'DEMO_DH04'
        THEN 'CASH'
        ELSE 'BANK_TRANSFER'
    END,
    'PAID',
    o.total_price,
    DATE_ADD(o.order_time, INTERVAL 30 MINUTE),
    o.id
FROM food_orders o
WHERE o.note IN ('DEMO_DH04', 'DEMO_DH05')
AND NOT EXISTS (
    SELECT 1 FROM payments p
    WHERE p.order_id = o.id
);
COMMIT;

-- Lay nam va thang hien tai
SET @report_year = YEAR(CURDATE());
SET @report_month = MONTH(CURDATE());

-- ======================================
-- 1. TAO KE HOACH DU LIEU TUNG THANG
-- ======================================
DROP TEMPORARY TABLE IF EXISTS demo_chart_months;
CREATE TEMPORARY TABLE demo_chart_months (
    month_no INT PRIMARY KEY,
    order_count INT,
    base_amount DOUBLE
);
INSERT INTO demo_chart_months VALUES
(1,  9,  220000),
(2,  11, 235000),
(3,  12, 250000),
(4,  14, 275000),
(5,  16, 295000),
(6,  15, 285000),
(7,  18, 315000),
(8,  16, 300000),
(9,  20, 340000),
(10, 22, 360000);
-- ======================================
-- 2. TAO SO THU TU DON HANG TU 1 DEN 22
-- ======================================
DROP TEMPORARY TABLE IF EXISTS demo_chart_numbers;
CREATE TEMPORARY TABLE demo_chart_numbers (
    n INT PRIMARY KEY
);
INSERT INTO demo_chart_numbers VALUES
(1),(2),(3),(4),(5),(6),
(7),(8),(9),(10),(11),(12),
(13),(14),(15),(16),(17),(18),
(19),(20),(21),(22);
-- ======================================
-- 3. TAO KHACH HANG DANH RIENG CHO DEMO
-- ======================================

START TRANSACTION;

INSERT INTO customers (name, phone, address)
SELECT
    'Khách hàng demo biểu đồ',
    '0000000099',
    'Dữ liệu minh họa'
WHERE NOT EXISTS (
    SELECT 1 FROM customers
    WHERE phone = '0000000099'
);
-- ======================================
-- 4. TAO DON HANG HOAN THANH
-- ======================================
INSERT INTO food_orders (
    order_type,
    order_time,
    status,
    total_price,
    note,
    delivery_address,
    customer_id,
    table_id
)
SELECT
    'DELIVERY',
    DATE_ADD(
        DATE_ADD(
            DATE_ADD(
                MAKEDATE(@report_year, 1),
                INTERVAL (m.month_no - 1) MONTH
            ),
            INTERVAL FLOOR(
                (n.n - 1) *
                CASE
                    WHEN m.month_no = @report_month
                    THEN GREATEST(DAY(CURDATE()) - 1, 1)
                    ELSE 27
                END / m.order_count
            ) DAY
        ),
        INTERVAL (9 + MOD(n.n, 11)) HOUR
    ),
    'COMPLETED',
    m.base_amount +
        MOD(n.n * 17 + m.month_no * 7, 8) * 15000,
    CONCAT(
        'DEMO_CHART_', @report_year, '_',
        LPAD(m.month_no, 2, '0'), '_',
        LPAD(n.n, 2, '0')
    ),
    'Địa chỉ khách hàng demo',
    c.id,
    NULL
FROM demo_chart_months m
JOIN demo_chart_numbers n
    ON n.n <= m.order_count
CROSS JOIN (
    SELECT id
    FROM customers
    WHERE phone = '0000000099'
    ORDER BY id
    LIMIT 1
) c
WHERE m.month_no <= @report_month

AND NOT EXISTS (
    SELECT 1
    FROM food_orders o
    WHERE o.note COLLATE utf8mb4_unicode_ci = CONCAT(
        'DEMO_CHART_', @report_year, '_',
        LPAD(m.month_no, 2, '0'), '_',
        LPAD(n.n, 2, '0')
    ) COLLATE utf8mb4_unicode_ci
);

-- ======================================
-- 5. TAO THANH TOAN THANH CONG
-- ======================================
INSERT INTO payments (
    payment_method,
    payment_status,
    amount,
    payment_time,
    order_id
)
SELECT
    CASE
        WHEN MOD(o.id, 2) = 0
        THEN 'BANK_TRANSFER'
        ELSE 'CASH'
    END,
    'PAID',
    o.total_price,
    DATE_ADD(o.order_time, INTERVAL 15 MINUTE),
    o.id
FROM food_orders o

WHERE o.note COLLATE utf8mb4_unicode_ci LIKE CONCAT(
    'DEMO_CHART_', @report_year, '%'
) COLLATE utf8mb4_unicode_ci

AND NOT EXISTS (
    SELECT 1
    FROM payments p
    WHERE p.order_id = o.id
);
COMMIT;



ROLLBACK;








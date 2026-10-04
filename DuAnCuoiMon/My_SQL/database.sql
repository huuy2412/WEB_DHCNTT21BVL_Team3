create database du_an_cuoi_mon
character set utf8mb4
collate utf8mb4_unicode_ci;
use du_an_cuoi_mon;

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


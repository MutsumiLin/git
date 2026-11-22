ALTER TABLE product ADD COLUMN revision BIGINT NOT NULL DEFAULT 0;
CREATE TABLE redemption_order (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 user_id BIGINT NOT NULL,
 merchant_id BIGINT NOT NULL,
 product_id BIGINT NOT NULL,
 client_request_id VARCHAR(64) NOT NULL,
 product_name VARCHAR(100) NOT NULL,
 points_price BIGINT NOT NULL,
 quantity INT NOT NULL,
 total_points BIGINT NOT NULL,
 status VARCHAR(16) NOT NULL,
 verify_code VARCHAR(64) NOT NULL UNIQUE,
 created_at DATETIME(6) NOT NULL,
 redeemed_at DATETIME(6) NULL,
 cancelled_at DATETIME(6) NULL,
 CONSTRAINT uk_order_request UNIQUE(user_id,client_request_id),
 CONSTRAINT fk_order_user FOREIGN KEY(user_id) REFERENCES sys_user(id),
 CONSTRAINT fk_order_merchant FOREIGN KEY(merchant_id) REFERENCES merchant(id),
 CONSTRAINT fk_order_product FOREIGN KEY(product_id) REFERENCES product(id),
 CONSTRAINT ck_order_quantity CHECK(quantity>=1 AND quantity<=10),
 CONSTRAINT ck_order_points CHECK(points_price>0 AND total_points=points_price*quantity),
 CONSTRAINT ck_order_state CHECK(status IN('PENDING','REDEEMED','CANCELLED'))
);
CREATE INDEX idx_order_user ON redemption_order(user_id,created_at,id);
CREATE INDEX idx_order_merchant ON redemption_order(merchant_id,status,id);

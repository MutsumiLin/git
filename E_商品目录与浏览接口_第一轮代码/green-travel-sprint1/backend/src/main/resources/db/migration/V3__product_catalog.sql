CREATE TABLE product (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  merchant_id BIGINT NOT NULL,
  sku VARCHAR(40) NOT NULL UNIQUE,
  name VARCHAR(100) NOT NULL,
  description VARCHAR(500) NOT NULL,
  category VARCHAR(30) NOT NULL,
  points_price BIGINT NOT NULL,
  stock INT NOT NULL,
  status VARCHAR(16) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  CONSTRAINT fk_product_merchant FOREIGN KEY(merchant_id) REFERENCES merchant(id),
  CONSTRAINT ck_product_price CHECK(points_price > 0),
  CONSTRAINT ck_product_stock CHECK(stock >= 0),
  CONSTRAINT ck_product_status CHECK(status IN('PUBLISHED','DRAFT','OFF_SHELF'))
);
CREATE INDEX idx_product_status ON product(status,id);
CREATE INDEX idx_product_merchant ON product(merchant_id,id);

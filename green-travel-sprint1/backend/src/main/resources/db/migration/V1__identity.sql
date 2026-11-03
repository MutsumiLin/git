CREATE TABLE merchant (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME(6) NOT NULL,
  CONSTRAINT uk_merchant_name UNIQUE (name)
);
CREATE TABLE sys_user (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(40) NOT NULL,
  password_hash VARCHAR(100) NOT NULL,
  role VARCHAR(16) NOT NULL,
  merchant_id BIGINT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at DATETIME(6) NOT NULL,
  CONSTRAINT uk_user_username UNIQUE (username),
  CONSTRAINT fk_user_merchant FOREIGN KEY (merchant_id) REFERENCES merchant(id),
  CONSTRAINT ck_user_role CHECK (role IN ('USER','ADMIN','MERCHANT')),
  CONSTRAINT ck_merchant_binding CHECK ((role = 'MERCHANT' AND merchant_id IS NOT NULL) OR (role <> 'MERCHANT' AND merchant_id IS NULL))
);

CREATE TABLE carbon_rule_version (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  version_name VARCHAR(40) NOT NULL UNIQUE,
  car_factor DECIMAL(9,6) NOT NULL,
  bus_factor DECIMAL(9,6) NOT NULL,
  metro_factor DECIMAL(9,6) NOT NULL,
  bike_factor DECIMAL(9,6) NOT NULL,
  points_per_kg INT NOT NULL,
  published_at DATETIME(6) NOT NULL,
  CONSTRAINT ck_rule_positive CHECK (car_factor >= 0 AND bus_factor >= 0 AND metro_factor >= 0 AND bike_factor >= 0 AND points_per_kg > 0)
);
INSERT INTO carbon_rule_version(version_name,car_factor,bus_factor,metro_factor,bike_factor,points_per_kg,published_at)
VALUES('simulation-v1',0.200000,0.080000,0.050000,0.000000,100,'2026-01-01 00:00:00');

CREATE TABLE point_account (
  user_id BIGINT PRIMARY KEY,
  balance BIGINT NOT NULL DEFAULT 0,
  updated_at DATETIME(6) NOT NULL,
  CONSTRAINT fk_account_user FOREIGN KEY(user_id) REFERENCES sys_user(id),
  CONSTRAINT ck_account_balance CHECK(balance >= 0)
);
CREATE TABLE trip_record (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  client_trip_id VARCHAR(64) NOT NULL,
  mode VARCHAR(12) NOT NULL,
  distance_km DECIMAL(8,2) NOT NULL,
  completed_at DATETIME(6) NOT NULL,
  source VARCHAR(20) NOT NULL,
  rule_version_id BIGINT NOT NULL,
  baseline_factor DECIMAL(9,6) NOT NULL,
  mode_factor DECIMAL(9,6) NOT NULL,
  points_per_kg INT NOT NULL,
  saved_carbon_kg DECIMAL(15,6) NOT NULL,
  earned_points BIGINT NOT NULL,
  created_at DATETIME(6) NOT NULL,
  CONSTRAINT uk_trip_request UNIQUE(user_id,client_trip_id),
  CONSTRAINT fk_trip_user FOREIGN KEY(user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_trip_rule FOREIGN KEY(rule_version_id) REFERENCES carbon_rule_version(id),
  CONSTRAINT ck_trip_mode CHECK(mode IN('BUS','METRO','BIKE')),
  CONSTRAINT ck_trip_distance CHECK(distance_km >= 0.01 AND distance_km <= 100.00),
  CONSTRAINT ck_trip_earned CHECK(saved_carbon_kg >= 0 AND earned_points >= 0)
);
CREATE INDEX idx_trip_user_created ON trip_record(user_id,created_at,id);
CREATE TABLE point_ledger (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  biz_type VARCHAR(32) NOT NULL,
  biz_id BIGINT NOT NULL,
  delta BIGINT NOT NULL,
  balance_after BIGINT NOT NULL,
  created_at DATETIME(6) NOT NULL,
  CONSTRAINT uk_ledger_business UNIQUE(user_id,biz_type,biz_id),
  CONSTRAINT fk_ledger_user FOREIGN KEY(user_id) REFERENCES sys_user(id),
  CONSTRAINT ck_ledger_balance CHECK(balance_after >= 0)
);
CREATE INDEX idx_ledger_user_created ON point_ledger(user_id,created_at,id);

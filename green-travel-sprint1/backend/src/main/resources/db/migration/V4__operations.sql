CREATE TABLE audit_log (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 actor_id BIGINT NOT NULL,
 action VARCHAR(50) NOT NULL,
 target VARCHAR(100) NOT NULL,
 created_at DATETIME(6) NOT NULL,
 CONSTRAINT fk_audit_actor FOREIGN KEY(actor_id) REFERENCES sys_user(id)
);
CREATE INDEX idx_audit_created ON audit_log(created_at,id);

# 第二轮数据库设计增量

V1至V3不改。V4增加审计表；V5增加订单并为商品增加revision。本清单依据本轮实际MySQL迁移后元数据生成；外键和检查约束以迁移SQL为准。

## audit_log

| 中文含义 | 字段 | 类型与精度 | 可空 | 默认值 | 键或索引 |
| --- | --- | --- | --- | --- | --- |
| 主键编号 | id | bigint | NO | 无显式默认值或NULL | PRI auto_increment |
| 操作人编号 | actor_id | bigint | NO | 无显式默认值或NULL | MUL  |
| 操作类型 | action | varchar(50) | NO | 无显式默认值或NULL | —  |
| 操作对象 | target | varchar(100) | NO | 无显式默认值或NULL | —  |
| 创建时间 | created_at | datetime(6) | NO | 无显式默认值或NULL | MUL  |

索引：

- fk_audit_actor：actor_id，复合索引位置1，非唯一。
- idx_audit_created：created_at，复合索引位置1，非唯一。
- idx_audit_created：id，复合索引位置2，非唯一。
- PRIMARY：id，复合索引位置1，唯一。

## product

| 中文含义 | 字段 | 类型与精度 | 可空 | 默认值 | 键或索引 |
| --- | --- | --- | --- | --- | --- |
| 主键编号 | id | bigint | NO | 无显式默认值或NULL | PRI auto_increment |
| 商户编号 | merchant_id | bigint | NO | 无显式默认值或NULL | MUL  |
| 商品编码 | sku | varchar(40) | NO | 无显式默认值或NULL | UNI  |
| 商品名称 | name | varchar(100) | NO | 无显式默认值或NULL | —  |
| 描述 | description | varchar(500) | NO | 无显式默认值或NULL | —  |
| 分类 | category | varchar(30) | NO | 无显式默认值或NULL | —  |
| 单件积分价或快照 | points_price | bigint | NO | 无显式默认值或NULL | —  |
| 可用库存 | stock | int | NO | 无显式默认值或NULL | —  |
| 业务状态 | status | varchar(16) | NO | 无显式默认值或NULL | MUL  |
| 创建时间 | created_at | datetime(6) | NO | 无显式默认值或NULL | —  |
| 商品数据版本 | revision | bigint | NO | 0 | —  |

索引：

- idx_product_merchant：merchant_id，复合索引位置1，非唯一。
- idx_product_merchant：id，复合索引位置2，非唯一。
- idx_product_status：status，复合索引位置1，非唯一。
- idx_product_status：id，复合索引位置2，非唯一。
- PRIMARY：id，复合索引位置1，唯一。
- sku：sku，复合索引位置1，唯一。

## redemption_order

| 中文含义 | 字段 | 类型与精度 | 可空 | 默认值 | 键或索引 |
| --- | --- | --- | --- | --- | --- |
| 主键编号 | id | bigint | NO | 无显式默认值或NULL | PRI auto_increment |
| 市民编号 | user_id | bigint | NO | 无显式默认值或NULL | MUL  |
| 商户编号 | merchant_id | bigint | NO | 无显式默认值或NULL | MUL  |
| 商品编号 | product_id | bigint | NO | 无显式默认值或NULL | MUL  |
| 客户端请求编号 | client_request_id | varchar(64) | NO | 无显式默认值或NULL | —  |
| 商品名快照 | product_name | varchar(100) | NO | 无显式默认值或NULL | —  |
| 单件积分价或快照 | points_price | bigint | NO | 无显式默认值或NULL | —  |
| 数量 | quantity | int | NO | 无显式默认值或NULL | —  |
| 消费积分总额 | total_points | bigint | NO | 无显式默认值或NULL | —  |
| 业务状态 | status | varchar(16) | NO | 无显式默认值或NULL | —  |
| 核销码 | verify_code | varchar(64) | NO | 无显式默认值或NULL | UNI  |
| 创建时间 | created_at | datetime(6) | NO | 无显式默认值或NULL | —  |
| 核销时间 | redeemed_at | datetime(6) | YES | 无显式默认值或NULL | —  |
| 取消时间 | cancelled_at | datetime(6) | YES | 无显式默认值或NULL | —  |

索引：

- fk_order_product：product_id，复合索引位置1，非唯一。
- idx_order_merchant：merchant_id，复合索引位置1，非唯一。
- idx_order_merchant：status，复合索引位置2，非唯一。
- idx_order_merchant：id，复合索引位置3，非唯一。
- idx_order_user：user_id，复合索引位置1，非唯一。
- idx_order_user：created_at，复合索引位置2，非唯一。
- idx_order_user：id，复合索引位置3，非唯一。
- PRIMARY：id，复合索引位置1，唯一。
- uk_order_request：user_id，复合索引位置1，唯一。
- uk_order_request：client_request_id，复合索引位置2，唯一。
- verify_code：verify_code，复合索引位置1，唯一。

## 约束与UML映射

RedemptionOrder映射redemption_order，用户、商户、商品关联映射非空外键；商品名称与单价复制到订单，避免修改商品后丢失历史。数量一至十，单价大于零，total_points=points_price×quantity。状态仅PENDING/REDEEMED/CANCELLED。用户加请求号唯一，随机核销码唯一；账户流水仍按用户、业务类型、业务编号唯一。

Product原字段约束不变，revision非空默认0，任何编辑、扣减或恢复库存都增加版本。AuditLog映射audit_log，actor_id关联sys_user，记录商品维护、规则发布和核销成功，随业务事务提交。MySQL InnoDB主键为聚集索引，业务唯一键为辅助唯一索引。核销不再次改变积分，清算尚未建表，第三轮另加新迁移。

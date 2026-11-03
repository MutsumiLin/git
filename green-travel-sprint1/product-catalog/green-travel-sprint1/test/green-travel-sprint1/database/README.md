# 数据库与初始化

默认使用 MySQL 8.4。表结构由后端启动时执行 Flyway 迁移建立，不需要手工复制 SQL 再执行一次。

迁移归属：

- A：`backend/src/main/resources/db/migration/V1__identity.sql`，商户与身份。
- D：`V2__trips_points.sql`，不可变规则、行程、积分账户和流水。
- E：`V3__product_catalog.sql`，商品与库存。

已应用的迁移文件不要改写；后续数据库变更新增 V4、V5 等文件。Flyway 保存迁移校验值。

演示数据由 F 的 `DemoDataInitializer` 在 `SEED_DEMO=true` 时添加。首次启动创建 citizen、citizen2、operator、merchant、merchant2 五个账号、两个商户和四个商品。用户初始余额均为零；重启不会清空行程、重置积分或覆盖现有商品。`DEMO_PASSWORD` 仅作用于首次创建的账号。

行程时间以 UTC 写入 DATETIME(6)，接口返回带时区时间，前端用北京时间展示。请求编号采用 ASCII 字母、数字、下划线或连字符，8—64 位，服务端统一转小写后进行幂等比较。

MySQL 自动化测试使用单独的 `green_travel_test` 库。测试会清理行程和积分夹具，禁止把业务库地址当测试库；测试代码也会拒绝清理名称不以 `_test` 结尾的 MySQL 数据库。

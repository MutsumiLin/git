# 第二轮升级与运行

## 将六包合并到第一轮仓库

先提交或保存本地修改，保持第一轮仓库干净。分别解压六包，将每包内green-travel-sprint1目录的内容合并到已有仓库根目录，覆盖对应文件，其他文件保留。不要删除第一轮源码、数据库或node_modules后再只放一个增量包。另一种方式是各成员在独立分支按清单提交，再由组长合并。

建议顺序A、D、E、B、C、F。所有包合并后，仓库根运行 `node tests/verify-bundle.mjs` 检查完整性。完整集成源码包用于新环境运行和核对，不代替各成员自己的提交。

## 数据库升级

继续使用第一轮的MySQL业务库和DB_URL、DB_USER、DB_PASSWORD。停止第一轮后端，备份业务库，启动第二轮；Flyway会先校验原V1至V3，再应用V4审计表及V5订单表和商品revision。V1至V3内容没有修改。不要删除flyway_schema_history、不要手动重跑建表、不要使用clean或重置库存余额来解决迁移错误。旧H2体验模式同样保留原.data目录。

环境和建库步骤见原deploy/RUNBOOK.md。本轮使用JDK21、Node24及原锁定依赖。backend目录启动：

```powershell
# 在同一终端保留或重新设置第一轮的DB_URL、DB_USER、DB_PASSWORD
$env:SEED_DEMO='true'
.\mvnw.cmd spring-boot:run
```

另一个终端进入frontend运行 `npm ci`、`npm run dev`。默认前端5173、后端8080，其他端口设置API_TARGET。访问http://127.0.0.1:5173。打包为 `.\mvnw.cmd package`，本轮jar名target/green-travel-0.2.0.jar。

## 演示一条完整流程

1. citizen登录，默认初次创建密码GreenDemo!2026；旧库自设密码保持原值。演示账号必须在SEED_DEMO=true时首次初始化。
2. 在初始simulation-v1规则下上报骑行10公里，得到200分；旧库已有其他规则则按当前规则计算。
3. 在商城打开100分环保袋，数量1确认兑换；可用积分减少100，库存减少1。
4. 我的兑换显示核销码。可先取消一次，验证退回100分、库存恢复、流水有消费和退款。
5. 再兑换一次，复制核销码。merchant登录工作台的订单核销页，粘贴并确认；重复核销不再次扣分。
6. citizen再登录查看已核销状态，此单不能取消。merchant2不能核销另一个商户订单。
7. operator在商品运营中编辑草稿或上下架，在积分规则发布新版本；历史行程和已发积分保持。
8. 重启后端，旧行程、订单、库存、余额和幂等编号仍保留。

## 测试

backend执行 `.\mvnw.cmd test`，默认隔离H2；真实MySQL测试按原RUNBOOK设置TEST_DB_URL、TEST_DB_USER、TEST_DB_PASSWORD、TEST_DB_DRIVER，数据库名必须以_test结尾。测试会清理该测试库的订单、审计、行程、流水和临时商品，不得使用业务库。

frontend执行 `npm test`、`npm run build`。仓库根执行 `node tests/sprint2-smoke.mjs --base-url http://127.0.0.1:8080 --output verification-local/sprint2.json`。该脚本会真实新增演示行程与两笔兑换，只在课程演示数据上运行。后端重启后加 `--verify-restart verification-local/sprint2.json` 验证保留记录。

价格或revision冲突409时刷新商品并重新核对。网络中断或服务错误时市民商城保留原编号，点击重试原兑换；不要删除浏览器存储来发起另一笔消费。商户列表最多显示最近100笔，完整分页与清算报表在后续轮次完善。

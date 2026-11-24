# 青行积分系统 第二轮迭代

项目采用Vue、Java Spring Boot、Spring JDBC与MySQL。第二轮在第一轮基础上实现商品维护、规则发布、积分兑换、订单取消、库存一致性及商户核销初版；清算和统计留到第三轮，第四轮交最终版。

## 先看这三份说明

- [六人固定分工](docs/team/SPRINT2_ROLES.md)：A工程权限与集成，B市民前端，C后台前端，D积分后端，E商品订单后端，F测试部署。
- [每人第二轮任务](docs/members/sprint2/)：各自的字母.md和字母-files.txt。
- [升级启动步骤](deploy/SPRINT2_RUNBOOK.md)：合并第一轮、保留数据库、运行演示。

## 目录与交付

backend为后端，frontend为前端，docs保存需求设计和真实验证，deploy保存部署说明，tests为HTTP与文件完整性脚本。六个成员包只包含本人本轮新增或修改文件，必须应用到已有第一轮仓库。包内green-travel-sprint1只是沿用原目录名；将其内容合并到仓库根即可。第二轮完整集成包可单独用于新环境运行和核对，实际协作按各自增量提交。

原docs/members/字母.md保留第一轮记录，本轮请使用docs/members/sprint2/字母.md。docs/design/SPRINT2_API.md是本轮新增接口契约，SPRINT2_TRANSACTIONS.md解释事务与事件。docs/requirements/SPRINT2.md给出验收边界；本轮验证见docs/testing/SPRINT2_VERIFICATION.md。

## 快速运行

准备JDK21、Node24和MySQL；沿用第一轮的数据库连接环境变量。在backend终端运行 `.\mvnw.cmd spring-boot:run`，在frontend终端运行 `npm ci`、`npm run dev`。默认访问http://127.0.0.1:5173；本地体验可按RUNBOOK使用H2文件模式，课程验收仍使用MySQL。

首次初始化设置SEED_DEMO=true。演示用户citizen、citizen2、operator、merchant、merchant2，首次默认密码GreenDemo!2026可由DEMO_PASSWORD配置；已有库不会重置密码、积分或库存。Flyway自动升级V4与V5，V1至V3保持原样。教学模拟系数不代表官方碳核算标准。

## 验证

backend中 `.\mvnw.cmd test`；frontend中 `npm test`、`npm run build`；仓库根 `node tests/verify-bundle.mjs`。第二轮HTTP演示使用 `node tests/sprint2-smoke.mjs`，它会真实新增演示数据，详细参数见部署说明。

本轮依然需要各成员实际审查、修改、验证并自行提交。AI使用见docs/AI_USAGE_SPRINT2.md；未替成员生成虚构commit、评审签字或个人总结。

# 青行积分 城市绿色出行系统

软件工程实验第一轮迭代代码，使用 Vue、Spring Boot、MySQL。六个成员包合并成同一个项目；每包是对应成员的文件增量，不是六套独立系统。

## 本轮可运行功能

- 市民、运营管理员、商户登录，Session 与 CSRF 校验。
- 公交、地铁、骑行模拟行程上报，精确计算模拟减排与整数积分。
- 同一用户同一请求编号仅发分一次；内容变化返回冲突。
- 同一事务保存行程、积分账户与流水，同步业务事件发放积分。
- 本人行程、余额和流水查询，数据隔离和分页。
- 用户浏览上架商品、查看详情；运营查看全部商品，商户只看本店商品。
- 运营与商户后台原型。兑换、核销、清算和维护入口明确标记为后续迭代。

## 快速开始

前置条件：JDK 21、Node.js 24、MySQL 8.4。Maven Wrapper 会下载并固定 Maven 3.9.16。

推荐按 [部署与启动说明](deploy/RUNBOOK.md) 启动 MySQL、后端和前端。数据库由 Flyway 建表，演示数据通过 `SEED_DEMO=true` 创建。已有账号不会因重启而覆盖密码或积分。

若只是先看界面，可在 backend 目录运行 `./mvnw spring-boot:run -Dspring-boot.run.profiles=demo`；Windows PowerShell 使用：

```powershell
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=demo'
```

这个可选体验模式使用 H2 文件库保存数据；课程的 MySQL 数据库验收采用默认 MySQL 配置，不能把 H2 测试当成 MySQL 验证。

另开一个终端，在 frontend 目录执行：

```powershell
npm ci
npm run dev
```

访问 http://127.0.0.1:5173 。前端开发代理连接 http://127.0.0.1:8080 。

| 角色 | 演示账号 | 首次创建时的默认演示密码 |
| --- | --- | --- |
| 市民 | citizen、citizen2 | GreenDemo!2026 |
| 运营 | operator | GreenDemo!2026 |
| 商户 | merchant、merchant2 | GreenDemo!2026 |

可以在首次初始化前设置 `DEMO_PASSWORD` 替换演示密码。演示账号只在启用演示数据时建立。

## 六人文件与提交

每个人先阅读 `docs/members/对应编号.md`。分工、GitHub 操作顺序与文件归属见 [协作提交说明](docs/team/GITHUB_COMMIT_GUIDE.md) 和 [文件归属清单](docs/team/FILE_OWNERSHIP.json)。六包中的业务文件互不重叠，按相同目录合并。

完整项目合并后再执行全量测试。中间提交可能尚缺其他成员依赖，应在分支或草稿 PR 中注明依赖。

## 测试

```powershell
cd backend
.\mvnw.cmd test
```

默认测试使用隔离 H2 内存库。真实 MySQL 测试的环境变量和独立测试库设置见部署说明。测试会清理测试库中的行程与积分数据。

```powershell
cd frontend
npm test
npm run build
```

项目根目录可执行 `node tests/smoke.mjs` 进行真实 HTTP 冒烟验证；验证脚本会创建一条实际的演示行程。实测记录见 `docs/testing/VERIFICATION.md`。

## 规则与工程资料

- [需求和用户故事](docs/requirements/SPRINT1.md)
- [接口契约](docs/design/API.md)
- [架构决策](docs/design/ADR.md)
- [数据库说明](database/README.md)
- `docs/models/`：UML 源文件。
- `docs/testing/`：基本路径、等价类、集成测试及实际验证记录。

所有碳系数均为教学模拟参数。每次计算保留规则和参数快照，不代表经认证的减排量。

本版代码由 Codex 辅助准备。成员应审查、修改、运行自己负责的内容后用本人 GitHub 身份提交，记录真实工作和 AI 使用情况；自动化测试记录不等于六人的人工验收记录。

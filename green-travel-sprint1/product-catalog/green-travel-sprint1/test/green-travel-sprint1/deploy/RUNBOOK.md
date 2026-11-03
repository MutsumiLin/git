# 第一轮部署与启动

## 环境

安装 JDK 21、Node.js 24、Git、MySQL 8.4。Docker 是可选的数据库启动方式，不是前置强制条件。Maven Wrapper 已包含，首次执行会下载 Maven 3.9.16，需要能够访问 Maven Central。

检查 `java -version`、`node -v`、`npm -v`、`git --version`。后端默认端口 8080，前端 5173，MySQL 默认 3306；本地端口不同可修改 DB_URL、SERVER_PORT 和前端 API_TARGET。

## 启动 MySQL

方式一：使用本机 MySQL，创建 green_travel 和 green_travel_test，建立仅能操作这两个库的课程账号。表结构由 Flyway 自动建立。

方式二：项目根目录执行：

```powershell
Copy-Item deploy/.env.example deploy/.env
# 编辑 deploy/.env，设置两个不同的本地数据库密码
docker compose --env-file deploy/.env -f deploy/compose.yml up -d
docker compose --env-file deploy/.env -f deploy/compose.yml ps
```

本地数据库数据存入 Docker volume，普通重启不会清空。不要使用清空 volume 的命令作为日常启动步骤。

## 启动后端

在第一个 PowerShell 终端进入项目 backend 目录。下面 URL 的弱化 TLS 选项仅用于本机课程数据库；远程连接按实际证书配置。

```powershell
$env:DB_URL='jdbc:mysql://127.0.0.1:3306/green_travel?connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true&allowPublicKeyRetrieval=true&useSSL=false'
$env:DB_USER='green_travel'
$env:DB_PASSWORD='填写你自己的本地数据库密码'
$env:SEED_DEMO='true'
.\mvnw.cmd spring-boot:run
```

访问 http://127.0.0.1:8080/api/health 应返回 UP。若显示连接失败，检查数据库端口、库名、账号密码及是否已启动；若端口占用，调整 SERVER_PORT 并同步前端代理。

可选的 H2 本地体验：不配置 MySQL 时，使用 `./mvnw spring-boot:run -Dspring-boot.run.profiles=demo`，Windows 为 `.\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=demo'`。文件数据在 backend/.data；这是体验模式，MySQL 验收仍应执行下一节实际数据库测试。

## 启动前端

第二个终端进入 frontend：

```powershell
npm ci
npm run dev
```

访问 http://127.0.0.1:5173 。后端使用其他端口时，启动前设置 `$env:API_TARGET='http://127.0.0.1:新的端口'`。浏览器统一使用同一个主机名访问，避免混用 localhost 和 127.0.0.1 导致会话 Cookie 不一致。

## 演示流程

1. citizen 登录，上报 BUS、10.00 公里、一个真实过去的完成时间。
2. 显示 120 积分、1.200 kgCO2e，查询行程与流水。
3. 刷新页面，退出再登录，确认数据保留。
4. 浏览商品和详情，核对“第二轮开放兑换”的迭代范围。
5. operator 登录读取全部商品，merchant 登录只看到自己的商品。
6. 停止并重新启动后端，再登录确认行程、积分没有清空。

## 自动化验证

backend 中 `./mvnw test`（Windows `.\mvnw.cmd test`）运行隔离 H2 测试。

真实 MySQL 测试需要先建立独立 green_travel_test；测试会清理该库的行程与积分夹具，数据库名必须以 `_test` 结尾：

```powershell
$env:TEST_DB_URL='jdbc:mysql://127.0.0.1:3306/green_travel_test?connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true&allowPublicKeyRetrieval=true&useSSL=false'
$env:TEST_DB_USER='green_travel'
$env:TEST_DB_PASSWORD='填写测试库密码'
$env:TEST_DB_DRIVER='com.mysql.cj.jdbc.Driver'
.\mvnw.cmd test
```

清除 TEST_DB_* 环境变量后再次运行，即恢复默认 H2 内存测试。不要把 DB_URL 的业务库直接复制到 TEST_DB_URL。

frontend 中运行 `npm test` 和 `npm run build`。项目根目录运行：

```powershell
node tests/smoke.mjs --output verification-local/http-smoke.json
```

后端重启后运行：

```powershell
node tests/smoke.mjs --verify-restart verification-local/http-smoke.json --output verification-local/restart.json
```

HTTP 脚本使用当前演示账号并新增一条真实模拟行程，不清空业务库。自己运行的 verification-local 目录已被 Git 忽略，可按需要把审核后的结果整理到报告。

## 构建与交付

backend 运行 `.\mvnw.cmd clean package`，启动生成的 `target/green-travel-0.1.0.jar`；frontend 运行 `npm ci` 和 `npm run build`。正式静态部署需要配置 SPA 路由回退及 /api 反向代理，避免直接打开 dist/index.html。

源代码提交不包含 node_modules、target、数据库文件和本地真实密码。日常备份使用 MySQL 的数据库备份工具保留业务库，恢复到单独环境先验证版本和迁移记录。

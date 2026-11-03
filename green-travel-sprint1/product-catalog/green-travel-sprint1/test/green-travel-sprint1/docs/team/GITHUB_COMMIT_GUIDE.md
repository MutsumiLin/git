# 六人代码包合并与 GitHub 提交

## 包的含义

六个 ZIP 是同一项目的六份文件增量。每个 ZIP 解压后都有 `green-travel-sprint1` 目录，取其中内容放到共同 Git 仓库根目录，保持 backend、frontend、docs 等相对路径。不要再把 ZIP 本身当源码提交，也不要把六包放成六个互不相通的子项目。

每个业务文件只归一个成员。`docs/members/A-files.txt` 至 `F-files.txt` 列出对应成员的全部提交路径，`FILE_OWNERSHIP.json` 为全组归属清单。包内不含 node_modules、target、数据库文件、个人 Git 配置或真实密码。

## 建仓库

1. A 在 GitHub 创建空仓库，例如 green-travel，邀请另外五人作为 Collaborator。
2. 每个人在自己电脑上用自己的 GitHub 账号 clone 仓库；不要共用一个人的账号提交。
3. 用本人真实姓名或常用 Git 名和已在 GitHub 验证的邮箱配置本仓库的提交身份：

```powershell
git config user.name "你自己的名字"
git config user.email "你自己已验证的邮箱"
```

不要把示例占位值直接提交为身份。代码基线由 AI 辅助准备，各人的审查、修改、运行、问题修复及 AI 使用记录要据实补充。

## 简单的顺序提交方式

建议合并顺序 A → D → E → C → B → F。每人先同步已提交内容，再把自己的增量复制进仓库，用自己的文件清单暂存。

A 在空仓库首次使用：

```powershell
git branch -M main
git add --pathspec-from-file=docs/members/A-files.txt
git diff --cached --stat
git commit -m "feat(identity): bootstrap project and role-based session login"
git push -u origin main
```

其他成员在自己克隆的仓库中，以下以 D 为例：

```powershell
git pull --ff-only origin main
# 将 D 包中的 green-travel-sprint1 内容复制到当前仓库根目录
git add --pathspec-from-file=docs/members/D-files.txt
git diff --cached --stat
git commit -m "feat(points): persist trips and grant idempotent carbon points"
git push origin main
```

轮到 E、C、B、F 时替换清单编号和提交说明。依次完成可减少初学阶段的推送冲突。每个人应检查暂存内容，实际修改了哪些文件就说明哪些工作。

## 使用分支和合并请求

如果教师希望展示 PR 协作，在复制自己包之前执行 `git switch -c feat/sprint1-d-points`，提交后 `git push -u origin feat/sprint1-d-points`，在 GitHub 发起 PR，由对应互审人检查。合并后下一个成员重新同步 main。

功能依赖可能尚未合入，完整构建和全量测试在六份集成后执行。D、E、F 的测试辅助与演示数据存在跨成员依赖；PR 说明应写明，而不是声称每个中间提交均可独立部署。

## 六个建议提交说明

| 成员 | 建议说明 | 主要内容 |
| --- | --- | --- |
| A | feat(identity): bootstrap project and role-based session login | 基础工程、认证权限、需求及架构 |
| B | feat(web): add citizen trip reporting and points pages | 用户端、统一请求、重试、预览、构建锁文件 |
| C | feat(workspace): add shared layout and role workbenches | 公共布局、图标、运营商户只读后台 |
| D | feat(points): persist trips and grant idempotent carbon points | 行程、规则、账户流水、同步事件与计算测试 |
| E | feat(catalog): add scoped product browsing endpoints | 商品结构、浏览、角色范围和测试 |
| F | test(sprint1): add fixtures integration checks and deployment | 演示数据、集成测试、部署、CI 与验证记录 |

## 最后验收

六人合并完，按 deploy/RUNBOOK.md 启动并测试。Windows 不必改 mvnw.cmd 权限；A 可执行 `git update-index --chmod=+x backend/mvnw`，便于 Linux/macOS 使用 shell Wrapper。

用 `git log --oneline --format="%h %an %s"` 查看真实提交，再按任务、审查、修复及验收记录组织互评。提交次数本身不等同贡献大小；如多人共同修改一个文件，后续提交据实体现实际改动。

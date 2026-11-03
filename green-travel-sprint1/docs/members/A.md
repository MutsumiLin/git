# 成员 A 第一轮代码说明

主责：组长与登录权限。姓名由组内替换，当前分工编号不代表已经完成了本人审查。

## 本包内容

基础工程、Spring Security 会话登录、CSRF、三类角色、用户创建事件、统一异常、第一轮需求与架构。

代码入口：backend/pom.xml；identity/SecurityConfig.java；AuthController.java；UserProvisioningService.java；V1__identity.sql。

完整文件列表见 `A-files.txt`。这是一份项目增量，需与其他五包按相同目录集成。

## 按顺序学习和操作

1. 先读需求、故事地图和架构决策，核对三类角色与本轮边界。
2. 阅读 SecurityConfig，解释会话、CSRF、密码散列和后端角色检查。
3. 检查 UserProvisioningService 事务及 UserProvisioned 事件，和 D 确认账户初始化。
4. 启动后端后用三种账号登录，验证错误密码、未登录和角色越权。
5. 与 F 核对部署环境和真实测试结果；据实补充需求评审与自己的修改。

## 验证与交接

在完整项目 backend 目录执行 .\mvnw.cmd "-Dtest=AuthFlowTest" test；检查健康接口和三种登录。

B、C 使用认证和身份接口；D 监听创建市民事件；F 使用创建服务初始化演示数据。

## 建议提交

在仓库根目录完成自己的修改和检查后：

```powershell
git add --pathspec-from-file=docs/members/A-files.txt
git diff --cached --stat
git commit -m "feat(identity): bootstrap project and role-based session login"
```

提交与推送前先按 docs/team/GITHUB_COMMIT_GUIDE.md 同步仓库。使用自己的身份，不把别人的文件一并暂存。

## 人工验收与 AI 使用记录

本包由 Codex 辅助生成并完成整套集成基线验证。下面内容由成员实际执行后填写：

- 实际姓名和日期：待填。
- 已阅读并能解释的代码：待填。
- 人工修改、原因与对应提交：待填。
- 本人运行的测试、实际结果及证据：待填。
- 互审人、问题与修复：待填。
- AI 建议中采用、修改、否决的内容及理由：待填。

课程个人总结二百至四百字，基于实际任务与收获撰写；不直接把这份说明当已完成的个人总结。

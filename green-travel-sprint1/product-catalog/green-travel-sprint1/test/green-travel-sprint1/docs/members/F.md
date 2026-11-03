# 成员 F 第一轮代码说明

主责：测试演示数据与部署。姓名由组内替换，当前分工编号不代表已经完成了本人审查。

## 本包内容

演示数据初始化、共享测试夹具、集成并发回滚测试、HTTP 脚本、部署、CI、全组文件归属及实测记录。

代码入口：backend/.../demo/DemoDataInitializer.java；TestSupport.java；integration/；tests/；deploy/；.github/workflows/verify.yml。

完整文件列表见 `F-files.txt`。这是一份项目增量，需与其他五包按相同目录集成。

## 按顺序学习和操作

1. 先读测试计划与基本路径图，区分自动化结果和组员人工验收。
2. 检查演示初始化只补缺失记录，不在重启时重置余额和库存。
3. 先运行 H2 快速测试，再用独立 MySQL _test 库验证实际事务、锁和唯一键。
4. 运行 HTTP 冒烟、后端重启验证，并核对部署说明能由其他成员复现。
5. 核对六包文件完整性和实际贡献资料，组织缺陷复测，补充自己的人工执行与 AI 使用记录。

## 验证与交接

完整项目运行 backend 测试、frontend 测试构建、node tests/smoke.mjs、node tests/verify-bundle.mjs；MySQL 步骤见 deploy/RUNBOOK.md。

接 A、D、E 服务及数据结构；邀请 B、C 做页面人工验收；所有人修复自己负责模块。

## 建议提交

在仓库根目录完成自己的修改和检查后：

```powershell
git add --pathspec-from-file=docs/members/F-files.txt
git diff --cached --stat
git commit -m "test(sprint1): add fixtures integration checks and deployment"
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

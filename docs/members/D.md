# 成员 D 第一轮代码说明

主责：行程碳计算与积分。姓名由组内替换，当前分工编号不代表已经完成了本人审查。

## 本包内容

规则和公式、行程持久化、账户和流水、同步奖励事件、幂等与行锁、积分查询、核心计算单元测试及模型。

代码入口：backend/src/main/java/edu/green/trip/ 和 points/；V2__trips_points.sql；CarbonCalculatorTest.java。

完整文件列表见 `D-files.txt`。这是一份项目增量，需与其他五包按相同目录集成。

## 按顺序学习和操作

1. 先手算公交、地铁和骑行十公里，核对单位、倍率和向下取整。
2. 阅读 CarbonCalculator 与单元测试，重点验证 0.01、0.09、0.25 和 100.00 公里。
3. 阅读 TripService，解释账户锁、当前读、参数规范化和相同编号内容比较。
4. 阅读 PointsEventListeners 和 PointWalletService，检查事务加入、唯一流水和余额一致性。
5. 与 F 跑并发和回滚测试，与 B 联调，确认历史参数快照并补个人审查记录。

## 验证与交接

backend 中 .\mvnw.cmd "-Dtest=CarbonCalculatorTest,TripFlowIntegrationTest,ConcurrencyIntegrationTest,RollbackIntegrationTest" test。

B 接用户接口；C 接规则；A 创建用户触发建账户；E 下一轮接积分扣减契约；F 负责跨模块复验。

## 建议提交

在仓库根目录完成自己的修改和检查后：

```powershell
git add --pathspec-from-file=docs/members/D-files.txt
git diff --cached --stat
git commit -m "feat(points): persist trips and grant idempotent carbon points"
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

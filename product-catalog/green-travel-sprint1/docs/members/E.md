# 成员 E 第一轮代码说明

主责：商品目录与浏览接口。姓名由组内替换，当前分工编号不代表已经完成了本人审查。

## 本包内容

商品结构和库存约束、用户列表详情、运营全部商品、商户所属商品、角色范围和查询测试。

代码入口：backend/src/main/java/edu/green/mall/；V3__product_catalog.sql；CatalogAccessTest.java。

完整文件列表见 `E-files.txt`。这是一份项目增量，需与其他五包按相同目录集成。

## 按顺序学习和操作

1. 阅读商品字段和数据库约束，解释积分价、库存、状态及商户关联。
2. 检查 Repository 的参数化查询，用户只看 PUBLISHED，商户范围来自会话。
3. 检查分页和商品详情，不泄露草稿商品。
4. 和 B、C 联调，验证用户三个上架商品、运营四个全部商品、商户三个本店商品。
5. 与 D 约定第二轮兑换事务边界，本轮不把尚未实现的兑换写成已经完成。

## 验证与交接

backend 中 .\mvnw.cmd "-Dtest=CatalogAccessTest" test；浏览器查看用户商城和两种后台商品。

B 使用列表详情；C 使用后台列表；F 初始化商品和做权限测试。第二轮 E 承担兑换与库存交易。

## 建议提交

在仓库根目录完成自己的修改和检查后：

```powershell
git add --pathspec-from-file=docs/members/E-files.txt
git diff --cached --stat
git commit -m "feat(catalog): add scoped product browsing endpoints"
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

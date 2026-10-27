# 成员 B 第一轮代码说明

主责：用户端页面。姓名由组内替换，当前分工编号不代表已经完成了本人审查。

## 本包内容

Vue 用户页面、路由、请求工具、登录交互、行程上报与重试、积分流水、商品浏览、响应式样式和前端测试。

代码入口：frontend/src/views/；components/TripForm.vue；api/client.js；stores/auth.js；utils/format.js；package-lock.json。

完整文件列表见 `B-files.txt`。这是一份项目增量，需与其他五包按相同目录集成。

## 按顺序学习和操作

1. 先掌握表单、列表、组件 props、事件和接口请求，阅读 api/client.js。
2. 运行 npm ci 和 npm run dev，先与 A 对接登录，登录后刷新 CSRF。
3. 用公交十公里验证上报和查分，检查页面结果来自后端。
4. 阅读 TripForm 的 pending 与 sessionStorage，解释为什么网络重试必须保留原编号和内容。
5. 检查积分、行程分页和商品详情；核对手机布局，并记录自己的人工测试与修改。

## 验证与交接

frontend 中 npm test、npm run build；浏览器以 citizen 登录，提交真实过去时间的公交十公里。

接 A 身份接口、D 行程积分接口、E 商品接口；使用 C 的 AppShell 和 GreenIcon。

## 建议提交

在仓库根目录完成自己的修改和检查后：

```powershell
git add --pathspec-from-file=docs/members/B-files.txt
git diff --cached --stat
git commit -m "feat(web): add citizen trip reporting and points pages"
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

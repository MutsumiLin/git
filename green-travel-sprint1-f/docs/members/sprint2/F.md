# 成员F第二轮任务与提交

固定角色：测试与部署。职责：共享测试夹具、跨模块集成测试、升级部署、验证证据与交付完整性。

## 这次实际交付

入口：backend/src/test/java/edu/green/integration/；TestSupport.java；tests/；deploy/；docs/testing/。精确文件清单：同目录 F-files.txt。本包是覆盖第一轮同路径文件的增量，路径不改名，原来的其他文件继续保留。六包需合并运行。

## 一步一步做

1. 在独立_test库运行全部测试。
2. 解释并发、重复请求和回滚案例。
3. 复现一轮到二轮升级。
4. 启动浏览器演示。
5. 核对六包与完整项目字节一致。

## 如何验证

全套 backend 测试及 frontend 构建；HTTP与浏览器复验。后端测试在 backend 执行 `.\mvnw.cmd "-Dtest=测试类名" test`；前端命令在 frontend 执行。先读 docs/team/SPRINT2_ROLES.md；接口见 docs/design/SPRINT2_API.md；启动见 deploy/SPRINT2_RUNBOOK.md。

## 如何提交

在已有第一轮仓库根目录打开终端。同步仓库后将本包内 green-travel-sprint1 的内容合并到仓库根，保留层级，不要在仓库里再套一层目录。先审查、修改和复验，再执行：

```powershell
git add --pathspec-from-file=docs/members/sprint2/F-files.txt
git diff --cached --stat
git commit -m "test(sprint2): cover commerce consistency upgrade and deployment"
git push -u origin HEAD
```

推送后由组长按实际分支合并。此命令只选择本轮本人负责文件；GitHub完整集成包仅供核对与运行，不由一个人一次提交全部。

## 本人据实补充

- 姓名、日期、分支与PR链接：待填写。
- 阅读并能解释的代码及设计：待填写。
- 实际修改、原因和commit：待填写。
- 本人运行的检查与结果：待填写。
- 互审问题及复测：待填写。
- AI建议的采用、修改、否决及理由：待填写。

交付包的Codex基线验证不代替六名成员本人审查。个人总结按指导书二百至四百字据实写，贡献比例基于真实记录，不预填虚构评价。

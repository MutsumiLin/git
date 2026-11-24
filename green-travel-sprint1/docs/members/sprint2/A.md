# 成员A第二轮任务与提交

固定角色：组长与工程权限。职责：工程基础、身份权限、审计公共服务、需求和接口规范、集成协调。

## 这次实际交付

入口：backend/pom.xml；common/AuditService.java；identity/AuthController.java；V4__operations.sql。精确文件清单：同目录 A-files.txt。本包是覆盖第一轮同路径文件的增量，路径不改名，原来的其他文件继续保留。六包需合并运行。

## 一步一步做

1. 核对第二轮需求和三类角色边界。
2. 解释审计为何加入业务事务。
3. 组织六包合并及接口评审。
4. 结合实际 Git 记录整理本轮回顾。

## 如何验证

AuthFlowTest。后端测试在 backend 执行 `.\mvnw.cmd "-Dtest=测试类名" test`；前端命令在 frontend 执行。先读 docs/team/SPRINT2_ROLES.md；接口见 docs/design/SPRINT2_API.md；启动见 deploy/SPRINT2_RUNBOOK.md。

## 如何提交

在已有第一轮仓库根目录打开终端。同步仓库后将本包内 green-travel-sprint1 的内容合并到仓库根，保留层级，不要在仓库里再套一层目录。先审查、修改和复验，再执行：

```powershell
git add --pathspec-from-file=docs/members/sprint2/A-files.txt
git diff --cached --stat
git commit -m "feat(platform): add operation audit and update sprint2 baseline"
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

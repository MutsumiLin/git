# 第二轮GitHub提交步骤

第一轮六包应先合并进同一仓库。以下每个人在自己电脑的仓库根目录执行，同步前先保存本地变更。主分支若不是main，替换为仓库实际默认分支。

```powershell
git switch main
git pull --ff-only
git switch -c feat/sprint2-a-platform
```

| 人员 | 本轮分支名 | 任务或PR标题 |
| --- | --- | --- |
| A | feat/sprint2-a-platform | 第二轮 工程基础 操作审计与集成维护 |
| B | feat/sprint2-b-citizen | 第二轮 市民兑换与订单页面 |
| C | feat/sprint2-c-workspace | 第二轮 商品维护与商户核销页面 |
| D | feat/sprint2-d-points | 第二轮 规则发布与积分消费退回 |
| E | feat/sprint2-e-commerce | 第二轮 商品库存与兑换订单接口 |
| F | test/sprint2-f-verification | 第二轮 一致性测试与升级部署 |

切分支后将自己的压缩包内green-travel-sprint1的内容合并到仓库根，按自己说明审查修改，再执行，以A为例：

```powershell
git add --pathspec-from-file=docs/members/sprint2/A-files.txt
git diff --cached --stat
git commit -m "feat(platform): add operation audit and update sprint2 baseline"
git push -u origin HEAD
```

然后在GitHub建立PR，由组长按A、D、E、B、C、F依次评审合并。B和C之间没有文件重叠，前后端依赖在全组合并后统一验证。若前一个PR修改了自己依赖的文件，先同步主分支并解决真实冲突，再测试。

不要把整个完整集成包由一个人一次提交，也不把复制文件动作写成已完成的独立开发贡献。实际阅读、修改、人工复验、问题修复和文档完善同样可以形成真实提交。文档、UML与测试都是交付文件，应按本人的清单一起提交。提交内容可先用git diff --cached检查，不包含本地密码、数据库文件、target或node_modules。

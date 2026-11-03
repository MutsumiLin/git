# 第一轮架构决策

## 架构比较

| 方案 | 对本组的支持 | 代价 | 决策 |
| --- | --- | --- | --- |
| 分层单体直接服务调用 | 事务简单、易部署 | 跨模块调用耦合逐渐增加 | 备选 |
| 模块化单体加进程内事件 | 单库事务，发分职责解耦，适合六人课内开发 | 同步处理占用请求线程，失败整体回滚 | 本轮采用 |
| 微服务加外部消息队列 | 可独立部署并扩展 | 网络故障、消息可靠性和分布式一致性更复杂 | 后续演进 |

## 模块与事件

identity 负责认证与会话，trip 负责上报和规则计算，points 负责账户和账本，mall 负责商品查询，demo 负责可选演示数据，common 负责响应与错误。

创建市民发布 UserProvisioned，积分模块建立零余额账户。上报保存后发布 TripAccepted，积分监听器同线程发分。Spring 默认同步事件没有消息中间件，不能宣称已实现跨服务可靠消息。

TripService 公共入口建立事务，账户通过主键 SELECT FOR UPDATE 加锁；同用户发分串行执行。幂等查询及账本业务查询为当前锁定读。监听器调用要求加入既有事务，异常向外传播，使三张业务表全部回滚。

## 持久层选型

本轮在指导书允许的 Spring Boot 与 MySQL 组合中采用 Spring JDBC，直接表达锁定 SQL 与参数绑定，方便初学者解释事务和幂等；未采用 JPA。后端仍按 Controller、Service、Repository 分层。

MySQL 是课程数据库配置。H2 文件模式用于低门槛本地体验，H2 内存模式用于快速测试；独立 MySQL 测试验证了实际唯一约束、行锁和事务，不能只用 mock 替代。

## 历史与迭代边界

规则在 Flyway 初始迁移中建立，本轮没有规则编辑 API。行程保存基准、方式系数、积分倍率及规则外键。模拟发分与已实现代码一致，商城兑换、核销和清算尚处于后续 Backlog。

## 参考资料

- [Spring 默认同步业务事件](https://docs.spring.io/spring-framework/reference/core/beans/context-introduction.html)
- [Spring 事务注解和代理边界](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html)
- [Spring Security CSRF](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html)
- [MySQL 锁定读取](https://dev.mysql.com/doc/refman/8.4/en/innodb-locking-reads.html)

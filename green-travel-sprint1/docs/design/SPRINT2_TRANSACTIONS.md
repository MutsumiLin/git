# 第二轮事务与事件设计

仍采用模块化单体加Spring同步事件。与直接跨模块调用相比，订单模块发布RedemptionChanged、积分模块监听后记账，减少订单模块对账本SQL的依赖。与微服务和外部消息队列相比，单库本地事务更便于课程内验证一致性，当前没有实现跨服务可靠消息。

兑换Service入口开启READ_COMMITTED事务，依次锁point_account，按(user_id,client_request_id)当前读查询原单，再锁product及其商户，保存订单、扣库存、同步发布事件。积分监听器要求加入已有事务、锁同一账户，按(user_id,biz_type,biz_id)检查唯一流水，更新余额并插入流水。监听器异常必须向外传播，不异步执行，不吞异常。取消采用账户、订单、商品顺序，按原快照退分。商户核销只需锁订单，不再扣分。

MySQL在默认REPEATABLE_READ下，未命中幂等查询可能产生间隙锁。跨用户竞争商品时曾暴露死锁，改为READ_COMMITTED后仍保留显式行锁和唯一约束，并用实际MySQL并发测试复验。该设计降低特定间隙锁竞争，不意味着生产环境绝无死锁。

商品编辑除悲观行锁外，比较页面读取时的revision；编辑、扣库存和恢复库存都自增revision。这样可以拒绝迟到的编辑保存，避免将已经售出的库存覆盖回旧值。兑换比较expectedPointsPrice，价格变化时要求重新确认。

设计类：ProductView对应商品实体视图，ProductManagementController为维护边界与控制入口；RedemptionController为HTTP边界，RedemptionService控制事务，RedemptionChanged为业务事件；PointWalletService负责账户和账本；RedemptionPointsListener为积分事件控制器；RuleController发布不可变规则；AuditService为同事务审计。鲁棒图、序列图、状态与活动图位于docs/models/sprint2。

参考：[MySQL事务隔离](https://dev.mysql.com/doc/refman/8.4/en/innodb-transaction-isolation-levels.html)、[Spring同步事件](https://docs.spring.io/spring-framework/reference/core/beans/context-introduction.html)。

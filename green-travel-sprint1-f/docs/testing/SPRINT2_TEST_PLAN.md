# 第二轮测试方案

继承第一轮碳计算的基本路径法与等价类用例，不把旧测试通过替代新业务验证。CarbonCalculator的条件判定和六条独立路径见原calculator-paths图和TEST_PLAN。新增测试对兑换、取消与核销事务采用下列有效、无效及状态划分。

| 测试目标 | 有效类 | 无效或异常类 | 自动化位置 |
| --- | --- | --- | --- |
| 数量与价格 | 一至十件、与读取价相同 | 零、十一、价格变化、额外身份字段 | CommerceFlowTest |
| 资金库存 | 余额足够、库存足够 | 积分不足、库存零、下架 | CommerceFlowTest |
| 幂等 | 同编号同内容重试 | 同编号不同数量；取消后重试不能重买 | CommerceFlowTest |
| 权限 | 本人订单、本店核销 | 他人读取取消、其他商户核销 | CommerceFlowTest |
| 状态 | PENDING到CANCELLED或REDEEMED | 已取消核销、已核销取消、重复取消核销 | CommerceFlowTest |
| 并发 | 同编号八请求仅一次扣分 | 两用户抢一个库存；取消核销竞争 | CommerceConcurrencyTest |
| 回滚 | 事件处理全部成功 | 已写消费或退款流水后注入异常 | CommerceRollbackTest |
| 规则 | 新规则用于新行程 | 重名、零倍率、负系数、越权 | RulePublicationTest、OperationsFlowTest |
| 库存编辑 | 当前revision保存 | 旧revision覆盖已售库存 | OperationsFlowTest |

回滚测试必须确认失败监听器执行前已存在对应账本记录，再验证整笔事务回滚；测试专用监听器不进入生产配置。并发测试使用真实Service与独立线程事务，不用mock替代数据库锁。

人工验收记录字段沿用指导书表4-1、4-2：编号、路径或功能、输入、期望、实际输出、发现问题、结果、时间、测试人。工具运行结果单列，个人测试人、真实时间和截图由成员亲自填写。尚未运行的GitHub Actions不能写成云端已通过。

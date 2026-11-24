# 第二轮接口契约

沿用Session与CSRF、统一响应{code,message,data}、401未登录、403角色或CSRF、400参数、404越权隐藏或不存在、409业务冲突。第一轮接口保持可用。写请求不能携带userId等额外字段。

| 方法 | 路径 | 角色 | 内容 |
| --- | --- | --- | --- |
| POST | /api/me/redemptions | USER | clientRequestId、productId、quantity、expectedPointsPrice；新建201，重试200；data={duplicate,order} |
| GET | /api/me/redemptions | USER | page从0，size一至100，返回PageResult |
| GET | /api/me/redemptions/{id} | USER | 只读本人订单 |
| POST | /api/me/redemptions/{id}/cancel | USER | 无请求体；退分和恢复库存 |
| POST | /api/admin/products 或 /api/merchant/products | 对应角色 | 新建商品，成功200 |
| PUT | 上述路径/{id} | 对应角色 | 修改商品，必须携带revision |
| GET | /api/merchant/redemptions | MERCHANT | 本店最近100笔订单 |
| POST | /api/merchant/redemptions/verify | MERCHANT | {verifyCode}；重复核销200 |
| POST | /api/admin/carbon-rules | ADMIN | versionName、四种factor、pointsPerKg |
| GET | /api/admin/carbon-rules | ADMIN | 规则版本历史 |

商品输入为merchantId、sku、name、description、category、pointsPrice、stock、status以及编辑时revision。sku为三至四十位大写字母数字下划线连字符；名称一至一百字符、描述一至五百、分类一至三十；积分价一至一千万，库存零至一百万。状态DRAFT/PUBLISHED/OFF_SHELF；商户ID与SKU不能编辑。商户请求merchantId省略或与会话相同。查询ProductView增加revision。运营页面从已有商品提取商户选项，本轮不创建新商户。

兑换请求例如：{"clientRequestId":"示例请用UUID替换","productId":1,"quantity":1,"expectedPointsPrice":100}。真实编号为八至六十四位ASCII字母数字下划线连字符，首位字母数字，服务端统一小写。相同用户的编号、商品、数量和确认单价一致才命中重试；发生价格变化须重新读取详情、确认后使用新编号。Order字段为id、productId、productName、pointsPrice、quantity、totalPoints、status、verifyCode、createdAt、redeemedAt、cancelledAt、merchantId。所有时间响应UTC偏移，市民页面显示上海时间。

规则输入模拟系数零至十、最多六位小数、倍率一至一万，版本三至四十位ASCII字母数字下划线连字符。允许交通系数高于基准，此时减排量按max(0,...)取零。历史版本不可编辑，重复版本名409。

积分流水增加REDEMPTION_DEBIT与REDEMPTION_REFUND；delta分别为负消费和正退回，bizId对应兑换订单ID。TRIP_REWARD仍对应行程ID。历史balanceAfter不是当前余额。

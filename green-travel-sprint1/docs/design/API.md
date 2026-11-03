# 第一轮接口契约

成功体 `{code:"OK",message:"成功",data:...}`；失败体保留 code、message，data 为 null。认证不足 401，角色不足或 CSRF 不正确 403，参数错误 400，不存在 404，请求冲突 409，未实现的方法 405，服务异常 500。

## 身份接口

GET `/api/auth/csrf`：匿名可访问，响应 data 包含 headerName、token，并建立或复用会话。

POST `/api/auth/login`：先获取 CSRF，使用 `application/x-www-form-urlencoded` 发送 username、password，并携带 CSRF 请求头和 Cookie。登录成功刷新 CSRF。响应为 id、username、role、merchantId。

POST `/api/auth/logout`：携带当前 CSRF，清除会话。GET `/api/me`：读取当前身份，不包含密码。

GET `/api/health`：匿名健康检查。GET `/api/carbon-rules/current`：任何已登录角色可查看当前教学模拟规则。

## 行程与积分

POST `/api/trips`：仅 USER。请求例子：

```json
{"clientTripId":"3a1e9c53-fb67-412b-afba-53c13bfa90b2","mode":"BUS","distanceKm":"10.00","completedAt":"2026-10-20T08:00:00+08:00"}
```

测试时把 completedAt 换为真实过去时间。请求不可加入 userId、earnedPoints 等额外字段。编号统一小写，里程规范到两位小数，时间规范到 UTC 微秒。

首次成功 201，重试命中 200。data 为 `{duplicate,trip}`，trip 包含 id、clientTripId、mode、distanceKm、completedAt、source、ruleVersion、savedCarbonKg、earnedPoints、balanceAfter、createdAt。balanceAfter 是该笔交易的历史变更后余额；页面当前余额重新读取 points 接口。

GET `/api/me/trips?page=0&size=10`；GET `/api/me/trips/{id}`；GET `/api/me/points`；GET `/api/me/point-ledger?page=0&size=10`。列表 data 包含 items、page、size、total。page 从零开始，size 为 1—100。

points 返回 balance、totalEarned、savedCarbonKg、tripCount。流水返回 id、bizType、bizId、delta、balanceAfter、createdAt。第一轮 bizType 只有 TRIP_REWARD。

## 商品和工作台

GET `/api/products`、`/api/products/{id}`：USER，仅已上架商品。GET `/api/admin/products`：ADMIN 全部商品。GET `/api/merchant/products`：MERCHANT，仅自己的商户范围。商户范围不接受客户端 merchantId 覆盖。

商品字段：id、merchantId、merchantName、sku、name、description、category、pointsPrice、stock、status。

商品维护、兑换、核销、清算均没有本轮写接口。页面相应入口标记为待后续迭代，不返回虚构成功响应。

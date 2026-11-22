package edu.green.mall;
import edu.green.common.*;
import edu.green.points.PointWalletService;
import jakarta.validation.constraints.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import java.sql.Statement;
import java.time.*;
import java.util.*;

@Service
public class RedemptionService {
 private final JdbcTemplate jdbc;private final PointWalletService wallets;private final ApplicationEventPublisher events;private final Clock clock;private final AuditService audit;
 public RedemptionService(JdbcTemplate j,PointWalletService w,ApplicationEventPublisher e,Clock c,AuditService a){jdbc=j;wallets=w;events=e;clock=c;audit=a;}
 public record Input(@NotNull @Pattern(regexp="[a-zA-Z0-9][a-zA-Z0-9_-]{7,63}") String clientRequestId,@Min(1) long productId,@Min(1) @Max(10) int quantity,@Min(1) @Max(10000000) long expectedPointsPrice){}
 public record Order(long id,long productId,String productName,long pointsPrice,int quantity,long totalPoints,String status,String verifyCode,
  OffsetDateTime createdAt,OffsetDateTime redeemedAt,OffsetDateTime cancelledAt,long merchantId){}
 private static OffsetDateTime utc(java.sql.ResultSet rs,String column)throws java.sql.SQLException{var t=rs.getObject(column,LocalDateTime.class);return t==null?null:t.atOffset(ZoneOffset.UTC);}
 private List<Order> rows(String tail,Object...args){return jdbc.query("SELECT * FROM redemption_order "+tail,(r,n)->new Order(r.getLong("id"),r.getLong("product_id"),r.getString("product_name"),r.getLong("points_price"),r.getInt("quantity"),r.getLong("total_points"),r.getString("status"),r.getString("verify_code"),utc(r,"created_at"),utc(r,"redeemed_at"),utc(r,"cancelled_at"),r.getLong("merchant_id")),args);}
 public Order owned(long user,long id){return rows("WHERE user_id=? AND id=?",user,id).stream().findFirst().orElseThrow(()->BusinessException.notFound("兑换订单不存在"));}
 public PageResult<Order> list(long user,int page,int size){PageResult.validate(page,size);return new PageResult<>(rows("WHERE user_id=? ORDER BY id DESC LIMIT ? OFFSET ?",user,size,(long)page*size),page,size,jdbc.queryForObject("SELECT COUNT(*) FROM redemption_order WHERE user_id=?",Long.class,user));}
 public List<Order> merchantOrders(long merchant){return rows("WHERE merchant_id=? ORDER BY id DESC LIMIT 100",merchant);}
 public record Result(boolean duplicate,Order order){}
 @Transactional(rollbackFor=Exception.class,isolation=Isolation.READ_COMMITTED)
 public Result create(long user,Input in){
  wallets.lockAccount(user);String key=in.clientRequestId().toLowerCase(Locale.ROOT);
  var old=rows("WHERE user_id=? AND client_request_id=? FOR UPDATE",user,key);
  if(!old.isEmpty()){
   Order o=old.getFirst();if(o.productId()!=in.productId() || o.quantity()!=in.quantity() || o.pointsPrice()!=in.expectedPointsPrice())throw BusinessException.conflict("重复请求编号的兑换内容不同");
   return new Result(true,o);
  }
  var found=jdbc.queryForList("SELECT p.*,m.status AS merchant_status FROM product p JOIN merchant m ON m.id=p.merchant_id WHERE p.id=? FOR UPDATE",in.productId());
  if(found.isEmpty())throw BusinessException.notFound("商品不存在");var p=found.getFirst();
  if(!"PUBLISHED".equals(p.get("status")) || !"ACTIVE".equals(p.get("merchant_status")))throw BusinessException.conflict("商品未上架或商户不可用");
  if(((Number)p.get("stock")).intValue()<in.quantity())throw BusinessException.conflict("商品库存不足");
  long price=((Number)p.get("points_price")).longValue();
  if(price!=in.expectedPointsPrice())throw BusinessException.conflict("商品积分价格已变化，请刷新商品详情后确认");
  long total=Math.multiplyExact(price,in.quantity());
  if(wallets.lockAccount(user)<total)throw BusinessException.conflict("可用积分不足");
  GeneratedKeyHolder keys=new GeneratedKeyHolder();
  jdbc.update(c->{var ps=c.prepareStatement("INSERT INTO redemption_order(user_id,merchant_id,product_id,client_request_id,product_name,points_price,quantity,total_points,status,verify_code,created_at) VALUES(?,?,?,?,?,?,?,?,'PENDING',?,?)",Statement.RETURN_GENERATED_KEYS);
   ps.setLong(1,user);ps.setLong(2,((Number)p.get("merchant_id")).longValue());ps.setLong(3,in.productId());ps.setString(4,key);ps.setString(5,p.get("name").toString());ps.setLong(6,price);ps.setInt(7,in.quantity());ps.setLong(8,total);ps.setString(9,UUID.randomUUID().toString());ps.setObject(10,now());return ps;},keys);
  long id=keys.getKey().longValue();jdbc.update("UPDATE product SET stock=stock-?,revision=revision+1 WHERE id=?",in.quantity(),in.productId());
  events.publishEvent(new RedemptionChanged(user,id,total,false));return new Result(false,owned(user,id));
 }
 @Transactional(rollbackFor=Exception.class,isolation=Isolation.READ_COMMITTED)
 public Order cancel(long user,long id){
  wallets.lockAccount(user);var orders=rows("WHERE user_id=? AND id=? FOR UPDATE",user,id);
  if(orders.isEmpty())throw BusinessException.notFound("兑换订单不存在");Order o=orders.getFirst();
  if(o.status().equals("CANCELLED"))return o;
  if(!o.status().equals("PENDING"))throw BusinessException.conflict("已核销订单不能取消");
  jdbc.queryForList("SELECT id FROM product WHERE id=? FOR UPDATE",o.productId());
  jdbc.update("UPDATE redemption_order SET status='CANCELLED',cancelled_at=? WHERE id=?",now(),id);
  jdbc.update("UPDATE product SET stock=stock+?,revision=revision+1 WHERE id=?",o.quantity(),o.productId());
  events.publishEvent(new RedemptionChanged(user,id,o.totalPoints(),true));return owned(user,id);
 }
 @Transactional(rollbackFor=Exception.class)
 public Order verify(long actor,long merchant,String code){
  var orders=rows("WHERE merchant_id=? AND verify_code=? FOR UPDATE",merchant,code);
  if(orders.isEmpty())throw BusinessException.notFound("本店未找到此核销码");Order o=orders.getFirst();
  if(o.status().equals("REDEEMED"))return o;
  if(!o.status().equals("PENDING"))throw BusinessException.conflict("已取消订单不能核销");
  jdbc.update("UPDATE redemption_order SET status='REDEEMED',redeemed_at=? WHERE id=?",now(),o.id());
  audit.record(actor,"ORDER_VERIFY",Long.toString(o.id()));return rows("WHERE id=?",o.id()).getFirst();
 }
 private LocalDateTime now(){return LocalDateTime.ofInstant(clock.instant(),ZoneOffset.UTC);}
}

package edu.green.mall;
import edu.green.common.*;
import edu.green.identity.DomainPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.util.*;

@RestController
public class ProductManagementController {
 private final JdbcTemplate jdbc;private final AuditService audit;private final Clock clock;
 public ProductManagementController(JdbcTemplate j,AuditService a,Clock c){jdbc=j;audit=a;clock=c;}
 public record Input(Long merchantId,@NotNull @Pattern(regexp="[A-Z0-9_-]{3,40}") String sku,
  @NotBlank @Size(max=100) String name,@NotBlank @Size(max=500) String description,@NotBlank @Size(max=30) String category,
  @Min(1) @Max(10000000) long pointsPrice,@Min(0) @Max(1000000) int stock,
  @NotNull @Pattern(regexp="DRAFT|PUBLISHED|OFF_SHELF") String status,@Min(0) Long revision){}
 private long merchant(DomainPrincipal actor,Long requested){
  if(actor.role().equals("MERCHANT")){
   if(requested!=null && !requested.equals(actor.merchantId()))throw BusinessException.notFound("商户不存在");
   return actor.merchantId();
  }
  if(requested==null)throw BusinessException.badRequest("请选择商户");return requested;
 }
 @PostMapping({"/api/admin/products","/api/merchant/products"}) @Transactional
 public ApiResponse<?> create(@AuthenticationPrincipal DomainPrincipal actor,@Valid @RequestBody Input in){
  long m=merchant(actor,in.merchantId());checkMerchant(m);
  if(jdbc.queryForObject("SELECT COUNT(*) FROM product WHERE sku=?",Long.class,in.sku())>0)throw BusinessException.conflict("SKU 已存在");
  jdbc.update("INSERT INTO product(merchant_id,sku,name,description,category,points_price,stock,status,created_at) VALUES(?,?,?,?,?,?,?,?,?)",m,in.sku(),in.name().trim(),in.description().trim(),in.category().trim(),in.pointsPrice(),in.stock(),in.status(),LocalDateTime.ofInstant(clock.instant(),ZoneOffset.UTC));
  audit.record(actor.id(),"PRODUCT_CREATE",in.sku());return ApiResponse.ok(Map.of("id",jdbc.queryForObject("SELECT id FROM product WHERE sku=?",Long.class,in.sku())));
 }
 @PutMapping({"/api/admin/products/{id}","/api/merchant/products/{id}"}) @Transactional
 public ApiResponse<?> update(@AuthenticationPrincipal DomainPrincipal actor,@PathVariable long id,@Valid @RequestBody Input in){
  var rows=jdbc.queryForList("SELECT merchant_id,sku,revision FROM product WHERE id=? FOR UPDATE",id);
  if(rows.isEmpty())throw BusinessException.notFound("商品不存在");
  long owner=((Number)rows.getFirst().get("merchant_id")).longValue();
  if(actor.role().equals("MERCHANT") && owner!=actor.merchantId())throw BusinessException.notFound("商品不存在");
  if((in.merchantId()!=null && in.merchantId()!=owner) || !in.sku().equals(rows.getFirst().get("sku")))throw BusinessException.badRequest("商品商户和 SKU 不可修改");
  if(in.revision()==null || in.revision()!=((Number)rows.getFirst().get("revision")).longValue())throw BusinessException.conflict("商品已被修改或发生兑换，请刷新后重新编辑");
  checkMerchant(owner);
  jdbc.update("UPDATE product SET name=?,description=?,category=?,points_price=?,stock=?,status=?,revision=revision+1 WHERE id=?",in.name().trim(),in.description().trim(),in.category().trim(),in.pointsPrice(),in.stock(),in.status(),id);
  audit.record(actor.id(),"PRODUCT_UPDATE",Long.toString(id));return ApiResponse.ok(Map.of("id",id));
 }
 private void checkMerchant(long id){if(jdbc.queryForObject("SELECT COUNT(*) FROM merchant WHERE id=? AND status='ACTIVE'",Long.class,id)==0)throw BusinessException.badRequest("商户不存在或不可用");}
}

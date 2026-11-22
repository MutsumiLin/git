package edu.green.mall;
import edu.green.TestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CommerceFlowTest extends TestSupport {
 private long bag(){return jdbc.queryForObject("SELECT id FROM product WHERE sku='ECO-BAG'",Long.class);}
 private int stock(){return jdbc.queryForObject("SELECT stock FROM product WHERE id=?",Integer.class,bag());}
 private Object body(String key,long product,int quantity){return Map.of("clientRequestId",key,"productId",product,"quantity",quantity,"expectedPointsPrice",100);}
 private Session funded()throws Exception{var s=login("citizen");report(s,trip(UUID.randomUUID().toString(),"BIKE","100.00"),201);return s;}
 private MvcResult write(Session s,String path,Object body,int status)throws Exception{return mvc.perform(post(path).session(s.http()).header(s.header(),s.token()).contentType("application/json").content(json.writeValueAsBytes(body))).andExpect(status().is(status)).andReturn();}
 private long create(Session s,String key,int quantity)throws Exception{return json.readTree(write(s,"/api/me/redemptions",body(key,bag(),quantity),201).getResponse().getContentAsString()).at("/data/order/id").asLong();}
 private String code(long id){return jdbc.queryForObject("SELECT verify_code FROM redemption_order WHERE id=?",String.class,id);}
 @Test void exchangeDebitsWalletAndStockAndCreatesOneLedger()throws Exception{
  var s=funded();long id=create(s,UUID.randomUUID().toString(),2);assertEquals(1800,balance("citizen"));assertEquals(18,stock());
  assertEquals(-200,jdbc.queryForObject("SELECT delta FROM point_ledger WHERE biz_type='REDEMPTION_DEBIT' AND biz_id=?",Long.class,id));
 }
 @Test void replayWithChangedPriceReturnsOriginalOrder()throws Exception{
  var s=funded();String key=UUID.randomUUID().toString();long id=create(s,key,1);jdbc.update("UPDATE product SET points_price=999 WHERE id=?",bag());
  var result=write(s,"/api/me/redemptions",body(key,bag(),1),200);assertTrue(json.readTree(result.getResponse().getContentAsString()).at("/data/duplicate").asBoolean());assertEquals(1900,balance("citizen"));assertEquals(19,stock());assertEquals(100,jdbc.queryForObject("SELECT total_points FROM redemption_order WHERE id=?",Long.class,id));
 }
 @Test void sameKeyDifferentQuantityConflicts()throws Exception{var s=funded();String k=UUID.randomUUID().toString();create(s,k,1);write(s,"/api/me/redemptions",body(k,bag(),2),409);assertEquals(1900,balance("citizen"));}
 @Test void insufficientBalanceDoesNotWrite()throws Exception{var s=login("citizen");write(s,"/api/me/redemptions",body(UUID.randomUUID().toString(),bag(),1),409);assertEquals(20,stock());assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM redemption_order",Integer.class));}
 @Test void insufficientStockDoesNotDebit()throws Exception{var s=funded();jdbc.update("UPDATE product SET stock=0 WHERE id=?",bag());write(s,"/api/me/redemptions",body(UUID.randomUUID().toString(),bag(),1),409);assertEquals(2000,balance("citizen"));}
 @Test void offShelfCannotBeExchanged()throws Exception{var s=funded();jdbc.update("UPDATE product SET status='OFF_SHELF' WHERE id=?",bag());write(s,"/api/me/redemptions",body(UUID.randomUUID().toString(),bag(),1),409);assertEquals(2000,balance("citizen"));}
 @Test void invalidQuantityAndExtraIdentityAreRejected()throws Exception{var s=funded();write(s,"/api/me/redemptions",body(UUID.randomUUID().toString(),bag(),0),400);write(s,"/api/me/redemptions",body(UUID.randomUUID().toString(),bag(),11),400);write(s,"/api/me/redemptions",Map.of("clientRequestId",UUID.randomUUID().toString(),"productId",bag(),"quantity",1,"userId",999),400);}
 @Test void cancelRefundsExactlyOnceAndRestoresStock()throws Exception{var s=funded();long id=create(s,UUID.randomUUID().toString(),2);write(s,"/api/me/redemptions/"+id+"/cancel",Map.of(),200);write(s,"/api/me/redemptions/"+id+"/cancel",Map.of(),200);assertEquals(2000,balance("citizen"));assertEquals(20,stock());assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM point_ledger WHERE biz_type='REDEMPTION_REFUND'",Integer.class));}
 @Test void anotherCitizenCannotReadOrCancel()throws Exception{long id=create(funded(),UUID.randomUUID().toString(),1);var other=login("citizen2");mvc.perform(get("/api/me/redemptions/"+id).session(other.http())).andExpect(status().isNotFound());write(other,"/api/me/redemptions/"+id+"/cancel",Map.of(),404);}
 @Test void onlyOwningMerchantCanVerifyAndRepeatedVerifyIsSafe()throws Exception{long id=create(funded(),UUID.randomUUID().toString(),1);write(login("merchant2"),"/api/merchant/redemptions/verify",Map.of("verifyCode",code(id)),404);var m=login("merchant");write(m,"/api/merchant/redemptions/verify",Map.of("verifyCode",code(id)),200);write(m,"/api/merchant/redemptions/verify",Map.of("verifyCode",code(id)),200);assertEquals(1900,balance("citizen"));assertEquals("REDEEMED",jdbc.queryForObject("SELECT status FROM redemption_order WHERE id=?",String.class,id));}
 @Test void redeemedCannotCancelAndCancelledCannotVerify()throws Exception{var s=funded();long id=create(s,UUID.randomUUID().toString(),1);write(login("merchant"),"/api/merchant/redemptions/verify",Map.of("verifyCode",code(id)),200);write(s,"/api/me/redemptions/"+id+"/cancel",Map.of(),409);long cancelled=create(s,UUID.randomUUID().toString(),1);write(s,"/api/me/redemptions/"+cancelled+"/cancel",Map.of(),200);write(login("merchant"),"/api/merchant/redemptions/verify",Map.of("verifyCode",code(cancelled)),409);}
 @Test void ledgerSumMatchesFinalBalanceAfterCancelAndSpend()throws Exception{var s=funded();long id=create(s,UUID.randomUUID().toString(),2);write(s,"/api/me/redemptions/"+id+"/cancel",Map.of(),200);create(s,UUID.randomUUID().toString(),1);assertEquals(balance("citizen"),jdbc.queryForObject("SELECT SUM(delta) FROM point_ledger WHERE user_id=?",Long.class,userId("citizen")));}
 @Test void changedPriceRequiresNewConfirmationWithoutDebiting()throws Exception{var s=funded();jdbc.update("UPDATE product SET points_price=150 WHERE id=?",bag());write(s,"/api/me/redemptions",body(UUID.randomUUID().toString(),bag(),1),409);assertEquals(2000,balance("citizen"));assertEquals(20,stock());}
 @Test void retryAfterCancellationReturnsCancelledOrderWithoutSpendingAgain()throws Exception{var s=funded();String key=UUID.randomUUID().toString();long id=create(s,key,1);write(s,"/api/me/redemptions/"+id+"/cancel",Map.of(),200);var r=write(s,"/api/me/redemptions",body(key,bag(),1),200);assertEquals("CANCELLED",json.readTree(r.getResponse().getContentAsString()).at("/data/order/status").asText());assertEquals(2000,balance("citizen"));assertEquals(20,stock());}
}

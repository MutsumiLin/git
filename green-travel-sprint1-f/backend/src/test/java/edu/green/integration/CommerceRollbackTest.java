package edu.green.integration;
import edu.green.TestSupport;
import edu.green.mall.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.*;
@Import(CommerceRollbackTest.Config.class)
class CommerceRollbackTest extends TestSupport {
 @Autowired RedemptionService service;@Autowired FailingListener fail;
 @TestConfiguration static class Config {@Bean FailingListener listener(JdbcTemplate jdbc){return new FailingListener(jdbc);}}
 static class FailingListener {
  final JdbcTemplate jdbc;AtomicBoolean enabled=new AtomicBoolean(false),observedLedger=new AtomicBoolean(false);
  FailingListener(JdbcTemplate j){jdbc=j;}
  @EventListener @Order(1000) void on(RedemptionChanged e){if(enabled.get()){
   observedLedger.set(jdbc.queryForObject("SELECT COUNT(*) FROM point_ledger WHERE biz_id=? AND biz_type=?",Integer.class,e.orderId(),e.refund()?"REDEMPTION_REFUND":"REDEMPTION_DEBIT")==1);
   throw new IllegalStateException("TEST_ONLY: injected failure after ledger update");
  }}
 }
 @Test void failedEventRollsBackStockOrderWalletAndLedgerThenRetryWorks()throws Exception{
  report(login("citizen"),trip(UUID.randomUUID().toString(),"BIKE","100.00"),201);long product=jdbc.queryForObject("SELECT id FROM product WHERE sku='ECO-BAG'",Long.class);var in=new RedemptionService.Input(UUID.randomUUID().toString(),product,1,100);
  fail.enabled.set(true);try{assertThrows(IllegalStateException.class,()->service.create(userId("citizen"),in));}finally{fail.enabled.set(false);}
  assertTrue(fail.observedLedger.get());assertEquals(2000,balance("citizen"));assertEquals(20,jdbc.queryForObject("SELECT stock FROM product WHERE id=?",Integer.class,product));assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM redemption_order",Integer.class));assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM point_ledger",Integer.class));
  assertFalse(service.create(userId("citizen"),in).duplicate());assertEquals(1900,balance("citizen"));
 }
 @Test void failedRefundRollsBackCancellationAndRetryRefundsOnce()throws Exception{
  report(login("citizen"),trip(UUID.randomUUID().toString(),"BIKE","100.00"),201);long p=jdbc.queryForObject("SELECT id FROM product WHERE sku='ECO-BAG'",Long.class);long user=userId("citizen");long id=service.create(user,new RedemptionService.Input(UUID.randomUUID().toString(),p,1,100)).order().id();
  fail.observedLedger.set(false);fail.enabled.set(true);try{assertThrows(IllegalStateException.class,()->service.cancel(user,id));}finally{fail.enabled.set(false);}
  assertTrue(fail.observedLedger.get());assertEquals(1900,balance("citizen"));assertEquals("PENDING",service.owned(user,id).status());assertEquals(19,jdbc.queryForObject("SELECT stock FROM product WHERE id=?",Integer.class,p));assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM point_ledger WHERE biz_type='REDEMPTION_REFUND'",Integer.class));
  service.cancel(user,id);service.cancel(user,id);assertEquals(2000,balance("citizen"));
 }
}

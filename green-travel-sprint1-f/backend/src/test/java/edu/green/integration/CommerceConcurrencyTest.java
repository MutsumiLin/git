package edu.green.integration;
import edu.green.TestSupport;
import edu.green.mall.RedemptionService;
import edu.green.common.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
class CommerceConcurrencyTest extends TestSupport {
 @Autowired RedemptionService service;
 private long product(){return jdbc.queryForObject("SELECT id FROM product WHERE sku='ECO-BAG'",Long.class);}
 @Test void twoUsersCannotOversellOneUnit()throws Exception{
  for(String name:List.of("citizen","citizen2"))report(login(name),trip(UUID.randomUUID().toString(),"BIKE","100.00"),201);
  jdbc.update("UPDATE product SET stock=1 WHERE id=?",product());var gate=new CountDownLatch(1);
  try(var pool=Executors.newFixedThreadPool(2)){
   List<Future<Boolean>> results=new ArrayList<>();for(String name:List.of("citizen","citizen2")){long id=userId(name);results.add(pool.submit(()->{gate.await();try{service.create(id,new RedemptionService.Input(UUID.randomUUID().toString(),product(),1,100));return true;}catch(BusinessException ex){return false;}}));}
   gate.countDown();int wins=0;for(var f:results)if(f.get(30,TimeUnit.SECONDS))wins++;assertEquals(1,wins);
  }
  assertEquals(0,jdbc.queryForObject("SELECT stock FROM product WHERE id=?",Integer.class,product()));assertEquals(3900,balance("citizen")+balance("citizen2"));assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM redemption_order",Integer.class));
 }
 @Test void concurrentSameRequestDebitsOnce()throws Exception{
  report(login("citizen"),trip(UUID.randomUUID().toString(),"BIKE","100.00"),201);var gate=new CountDownLatch(1);var input=new RedemptionService.Input(UUID.randomUUID().toString(),product(),1,100);long user=userId("citizen");
  try(var pool=Executors.newFixedThreadPool(8)){List<Future<RedemptionService.Result>> results=new ArrayList<>();for(int i=0;i<8;i++)results.add(pool.submit(()->{gate.await();return service.create(user,input);}));gate.countDown();int fresh=0;for(var f:results)if(!f.get(30,TimeUnit.SECONDS).duplicate())fresh++;assertEquals(1,fresh);}
  assertEquals(1900,balance("citizen"));assertEquals(19,jdbc.queryForObject("SELECT stock FROM product WHERE id=?",Integer.class,product()));
 }
 @Test void cancellationAndVerificationHaveExactlyOneTerminalState()throws Exception{
  report(login("citizen"),trip(UUID.randomUUID().toString(),"BIKE","100.00"),201);long user=userId("citizen"),actor=userId("merchant"),merchant=users.findByUsername("merchant").orElseThrow().merchantId();var o=service.create(user,new RedemptionService.Input(UUID.randomUUID().toString(),product(),1,100)).order();var gate=new CountDownLatch(1);
  try(var pool=Executors.newFixedThreadPool(2)){
   var cancel=pool.submit(()->{gate.await();try{service.cancel(user,o.id());return true;}catch(BusinessException e){return false;}});
   var verify=pool.submit(()->{gate.await();try{service.verify(actor,merchant,o.verifyCode());return true;}catch(BusinessException e){return false;}});
   gate.countDown();assertNotEquals(cancel.get(30,TimeUnit.SECONDS),verify.get(30,TimeUnit.SECONDS));
  }
  String status=service.owned(user,o.id()).status();assertTrue(status.equals("REDEEMED")||status.equals("CANCELLED"));assertEquals(status.equals("CANCELLED")?2000:1900,balance("citizen"));assertEquals(status.equals("CANCELLED")?20:19,jdbc.queryForObject("SELECT stock FROM product WHERE id=?",Integer.class,product()));
 }
}

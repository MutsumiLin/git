package edu.green.trip;
import edu.green.TestSupport;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
class RulePublicationTest extends TestSupport {
 private Map<String,Object> input(){return new HashMap<>(Map.of("versionName","test-published","carFactor","0.2","busFactor","0.08","metroFactor","0.05","bikeFactor","0","pointsPerKg",100));}
 private void publish(String user,Object in,int expected)throws Exception{var s=login(user);mvc.perform(post("/api/admin/carbon-rules").session(s.http()).header(s.header(),s.token()).contentType("application/json").content(json.writeValueAsBytes(in))).andExpect(status().is(expected));}
 @Test void duplicateVersionIsRejectedWithoutOverwriting()throws Exception{publish("operator",input(),200);publish("operator",input(),409);assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM carbon_rule_version",Integer.class));}
 @Test void nonAdminCannotPublish()throws Exception{publish("merchant",input(),403);publish("citizen",input(),403);}
 @Test void zeroMultiplierAndNegativeFactorAreRejected()throws Exception{var in=input();in.put("pointsPerKg",0);publish("operator",in,400);in=input();in.put("carFactor","-0.1");publish("operator",in,400);}
 @Test void largerModeFactorProducesZeroReward()throws Exception{var in=input();in.put("busFactor","0.3");publish("operator",in,200);var s=login("citizen");report(s,trip(UUID.randomUUID().toString(),"BUS","10.00"),201);assertEquals(0,balance("citizen"));assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM point_ledger",Integer.class));}
}

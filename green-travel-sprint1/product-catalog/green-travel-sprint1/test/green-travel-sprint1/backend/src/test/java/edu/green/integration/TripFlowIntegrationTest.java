package edu.green.integration;

import edu.green.TestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TripFlowIntegrationTest extends TestSupport {
    @Test void tenKmBusPersistsTripAccountAndLedger() throws Exception {
        var session=login("citizen");
        var result=report(session,trip(),201);
        var data=json.readTree(result.getResponse().getContentAsString()).path("data");
        assertFalse(data.path("duplicate").asBoolean());
        assertEquals(120,data.path("trip").path("earnedPoints").asLong());
        assertEquals(120,balance("citizen"));
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM trip_record",Integer.class));
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM point_ledger",Integer.class));
        mvc.perform(get("/api/me/points").session(session.http())).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balance").value(120)).andExpect(jsonPath("$.data.tripCount").value(1));
    }
    @Test void exactReplayReturnsSameResultWithoutRewardingAgain() throws Exception {
        var session=login("citizen");var input=trip();
        var first=json.readTree(report(session,input,201).getResponse().getContentAsString()).path("data").path("trip");
        var second=json.readTree(report(session,input,200).getResponse().getContentAsString()).path("data");
        assertTrue(second.path("duplicate").asBoolean());assertEquals(first,second.path("trip"));assertEquals(120,balance("citizen"));
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM point_ledger",Integer.class));
    }
    @Test void changedReplayIsConflictAndBalanceStaysUnchanged() throws Exception {
        var session=login("citizen");var input=new HashMap<>(trip());report(session,input,201);
        input.put("distanceKm","10.01");report(session,input,409);assertEquals(120,balance("citizen"));
    }
    @Test void zeroRewardTripStillHasTraceableLedger() throws Exception {
        var session=login("citizen");report(session,trip(UUID.randomUUID().toString(),"BUS","0.01"),201);
        assertEquals(0,balance("citizen"));assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM point_ledger WHERE delta=0",Integer.class));
    }
    @ParameterizedTest @ValueSource(strings={"0","-1","100.01","10.001","abc",""})
    void invalidDistanceEquivalenceClassesDoNotWriteAnything(String distance) throws Exception {
        var session=login("citizen");report(session,trip(UUID.randomUUID().toString(),"BUS",distance),400);
        assertEquals(0,balance("citizen"));assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM trip_record",Integer.class));
    }
    @Test void futureTimeUnknownModeAndForgedIdentityAreRejected() throws Exception {
        var session=login("citizen");var input=new HashMap<>(trip());
        input.put("completedAt",Instant.now().plusSeconds(3600).toString());report(session,input,400);
        input=new HashMap<>(trip());input.put("mode","CAR");report(session,input,400);
        input=new HashMap<>(trip());input.put("userId",userId("citizen2"));report(session,input,400);
        assertEquals(0,balance("citizen"));assertEquals(0,balance("citizen2"));
    }
    @Test void dataOwnershipIsEnforcedEvenWithQueryParameters() throws Exception {
        var citizen=login("citizen");var other=login("citizen2");
        long id=json.readTree(report(citizen,trip(),201).getResponse().getContentAsString()).path("data").path("trip").path("id").asLong();
        mvc.perform(get("/api/me/trips/"+id).session(other.http())).andExpect(status().isNotFound());
        mvc.perform(get("/api/me/trips").param("userId",Long.toString(userId("citizen"))).session(other.http()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(0));
        mvc.perform(get("/api/me/points").session(other.http())).andExpect(status().isOk()).andExpect(jsonPath("$.data.balance").value(0));
    }
    @Test void immutableRuleSnapshotKeepsOldPoints() throws Exception {
        var session=login("citizen");report(session,trip(),201);
        jdbc.update("INSERT INTO carbon_rule_version(version_name,car_factor,bus_factor,metro_factor,bike_factor,points_per_kg,published_at) VALUES('test-v2',0.2,0.1,0.05,0,100,?)",java.time.LocalDateTime.now(java.time.ZoneOffset.UTC));
        report(session,trip(),201);
        assertEquals(220,balance("citizen"));
        assertEquals(120,jdbc.queryForObject("SELECT earned_points FROM trip_record ORDER BY id LIMIT 1",Long.class));
        assertEquals(100,jdbc.queryForObject("SELECT earned_points FROM trip_record ORDER BY id DESC LIMIT 1",Long.class));
    }
    @Test void csrfIsRequiredForReport() throws Exception {
        var session=login("citizen");
        mvc.perform(post("/api/trips").session(session.http()).contentType("application/json").content(json.writeValueAsBytes(trip())))
                .andExpect(status().isForbidden());
        assertEquals(0,balance("citizen"));
    }
    @Test void loginAgainKeepsPersistedBalanceAndLedger() throws Exception {
        var first=login("citizen");report(first,trip(),201);
        mvc.perform(post("/api/auth/logout").session(first.http()).header(first.header(),first.token())).andExpect(status().isOk());
        var second=login("citizen");
        mvc.perform(get("/api/me/point-ledger").session(second.http())).andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(1));
        assertEquals(120,balance("citizen"));
    }
    @Test void balanceAlwaysMatchesLedgerSumForMultipleModes() throws Exception {
        var session=login("citizen");
        for(String mode:new String[]{"BUS","METRO","BIKE"})report(session,trip(UUID.randomUUID().toString(),mode,"10.00"),201);
        assertEquals(470,balance("citizen"));
        assertEquals(balance("citizen"),jdbc.queryForObject("SELECT SUM(delta) FROM point_ledger WHERE user_id=?",Long.class,userId("citizen")));
    }
}

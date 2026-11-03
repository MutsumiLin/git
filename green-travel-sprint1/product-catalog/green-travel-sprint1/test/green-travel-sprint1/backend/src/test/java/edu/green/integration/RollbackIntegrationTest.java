package edu.green.integration;

import edu.green.TestSupport;
import edu.green.trip.TripAccepted;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

@Import(RollbackIntegrationTest.FailureConfig.class)
class RollbackIntegrationTest extends TestSupport {
    @Autowired FailureListener failure;
    static class FailureListener {
        final AtomicBoolean enabled=new AtomicBoolean(false);
        @EventListener @Order(100)
        public void failAfterReward(TripAccepted event) {
            if(enabled.getAndSet(false))throw new IllegalStateException("Test-only failure after points listener");
        }
    }
    @TestConfiguration static class FailureConfig {
        @Bean FailureListener failureListener() {return new FailureListener();}
    }
    @Test void failureAfterCreditRollsBackTripAccountAndLedgerThenRetrySucceeds() throws Exception {
        var session=login("citizen");var input=trip();
        failure.enabled.set(true);report(session,input,500);
        assertEquals(0,balance("citizen"));
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM trip_record",Integer.class));
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM point_ledger",Integer.class));
        report(session,input,201);assertEquals(120,balance("citizen"));
    }
}

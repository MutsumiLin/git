package edu.green.integration;

import edu.green.TestSupport;
import edu.green.trip.ReportTripRequest;
import edu.green.trip.TransportMode;
import edu.green.trip.TripService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class ConcurrencyIntegrationTest extends TestSupport {
    @Autowired TripService trips;
    private ArrayList<TripService.ReportResult> simultaneous(boolean sameRequest) throws Exception {
        int count=8;long id=userId("citizen");
        var time=OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(5).truncatedTo(ChronoUnit.MILLIS);
        String shared=UUID.randomUUID().toString();
        var gate=new CountDownLatch(1);
        try(var pool=Executors.newFixedThreadPool(count)) {
            var pending=new ArrayList<Future<TripService.ReportResult>>();
            for(int i=0;i<count;i++) {
                var request=new ReportTripRequest(sameRequest?shared:UUID.randomUUID().toString(),TransportMode.BUS,new BigDecimal("10.00"),time);
                pending.add(pool.submit(()->{gate.await();return trips.report(id,request);}));
            }
            gate.countDown();
            var result=new ArrayList<TripService.ReportResult>();
            for(var future:pending)result.add(future.get(30,TimeUnit.SECONDS));
            return result;
        }
    }
    @Test void concurrentReplayCreatesOneReward() throws Exception {
        var results=simultaneous(true);
        assertEquals(1,results.stream().filter(r->!r.duplicate()).count());
        assertEquals(1,results.stream().map(r->r.trip().id()).distinct().count());
        assertEquals(120,balance("citizen"));
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM trip_record",Integer.class));
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM point_ledger",Integer.class));
    }
    @Test void concurrentDistinctTripsDoNotLoseUpdates() throws Exception {
        simultaneous(false);
        assertEquals(960,balance("citizen"));
        assertEquals(8,jdbc.queryForObject("SELECT COUNT(*) FROM point_ledger",Integer.class));
        assertEquals(960,jdbc.queryForObject("SELECT SUM(delta) FROM point_ledger",Long.class));
    }
}

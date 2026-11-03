package edu.green.trip;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcTripRepository {
    private final JdbcTemplate jdbc;
    public JdbcTripRepository(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    public CarbonRule currentRule() {
        return jdbc.query("SELECT * FROM carbon_rule_version ORDER BY published_at DESC,id DESC LIMIT 1",(rs,n) ->
                new CarbonRule(rs.getLong("id"),rs.getString("version_name"),rs.getBigDecimal("car_factor"),rs.getBigDecimal("bus_factor"),
                        rs.getBigDecimal("metro_factor"),rs.getBigDecimal("bike_factor"),rs.getInt("points_per_kg"))).stream().findFirst().orElseThrow();
    }
    public record StoredTrip(long id,TransportMode mode,BigDecimal distance,LocalDateTime completedAt) { }
    public Optional<StoredTrip> findRequestForUpdate(long userId,String requestId) {
        return jdbc.query("SELECT id,mode,distance_km,completed_at FROM trip_record WHERE user_id=? AND client_trip_id=? FOR UPDATE",
                (rs,n) -> new StoredTrip(rs.getLong("id"),TransportMode.valueOf(rs.getString("mode")),rs.getBigDecimal("distance_km"),
                        rs.getObject("completed_at",LocalDateTime.class)),userId,requestId).stream().findFirst();
    }
    public long insert(long userId,String requestId,TransportMode mode,BigDecimal distance,LocalDateTime completed,
                       CarbonRule rule,CarbonCalculator.Result result,LocalDateTime now) {
        GeneratedKeyHolder keys=new GeneratedKeyHolder();
        jdbc.update(connection -> {
            var ps=connection.prepareStatement("""
                INSERT INTO trip_record(user_id,client_trip_id,mode,distance_km,completed_at,source,rule_version_id,
                  baseline_factor,mode_factor,points_per_kg,saved_carbon_kg,earned_points,created_at)
                VALUES(?,?,?,?,?,'SIMULATION',?,?,?,?,?,?,?)
                """,Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1,userId);ps.setString(2,requestId);ps.setString(3,mode.name());ps.setBigDecimal(4,distance);
            ps.setObject(5,completed);ps.setLong(6,rule.id());ps.setBigDecimal(7,rule.carFactor());ps.setBigDecimal(8,rule.factor(mode));
            ps.setInt(9,rule.pointsPerKg());ps.setBigDecimal(10,result.savedCarbonKg());ps.setLong(11,result.earnedPoints());ps.setObject(12,now);
            return ps;
        },keys);
        return keys.getKey().longValue();
    }
    private static final String VIEW_SQL="""
        SELECT t.*,r.version_name,l.balance_after
        FROM trip_record t JOIN carbon_rule_version r ON r.id=t.rule_version_id
        JOIN point_ledger l ON l.user_id=t.user_id AND l.biz_type='TRIP_REWARD' AND l.biz_id=t.id
        """;
    private static final RowMapper<TripView> MAPPER=(rs,n) -> new TripView(rs.getLong("id"),rs.getString("client_trip_id"),
            TransportMode.valueOf(rs.getString("mode")),rs.getBigDecimal("distance_km"),rs.getObject("completed_at",LocalDateTime.class).atOffset(ZoneOffset.UTC),
            rs.getString("source"),rs.getString("version_name"),rs.getBigDecimal("saved_carbon_kg"),rs.getLong("earned_points"),
            rs.getLong("balance_after"),rs.getObject("created_at",LocalDateTime.class).atOffset(ZoneOffset.UTC));
    public Optional<TripView> findView(long userId,long tripId) {
        return jdbc.query(VIEW_SQL+" WHERE t.user_id=? AND t.id=?",MAPPER,userId,tripId).stream().findFirst();
    }
    public List<TripView> list(long userId,int page,int size) {
        return jdbc.query(VIEW_SQL+" WHERE t.user_id=? ORDER BY t.created_at DESC,t.id DESC LIMIT ? OFFSET ?",MAPPER,userId,size,(long)page*size);
    }
    public long count(long userId) { return jdbc.queryForObject("SELECT COUNT(*) FROM trip_record WHERE user_id=?",Long.class,userId); }
}

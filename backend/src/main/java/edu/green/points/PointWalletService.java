package edu.green.points;

import edu.green.common.BusinessException;
import edu.green.common.PageResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class PointWalletService {
    private final JdbcTemplate jdbc;
    private final Clock clock;
    public PointWalletService(JdbcTemplate jdbc,Clock clock) { this.jdbc=jdbc;this.clock=clock; }
    @Transactional(propagation=Propagation.MANDATORY)
    public void initialize(long userId) {
        jdbc.update("INSERT INTO point_account(user_id,balance,updated_at) VALUES(?,0,?)",userId,now());
    }
    @Transactional(propagation=Propagation.MANDATORY)
    public long lockAccount(long userId) {
        return jdbc.query("SELECT balance FROM point_account WHERE user_id=? FOR UPDATE",(rs,n)->rs.getLong(1),userId)
                .stream().findFirst().orElseThrow(()->BusinessException.notFound("积分账户不存在"));
    }
    @Transactional(propagation=Propagation.MANDATORY)
    public void creditTrip(long userId,long tripId,long points) {
        long balance=lockAccount(userId);
        if(points<0) throw new IllegalArgumentException("奖励积分不可为负");
        var existing=jdbc.query("SELECT id FROM point_ledger WHERE user_id=? AND biz_type='TRIP_REWARD' AND biz_id=? FOR UPDATE",(rs,n)->rs.getLong(1),userId,tripId);
        if(!existing.isEmpty()) return;
        long after=Math.addExact(balance,points);
        jdbc.update("UPDATE point_account SET balance=?,updated_at=? WHERE user_id=?",after,now(),userId);
        jdbc.update("INSERT INTO point_ledger(user_id,biz_type,biz_id,delta,balance_after,created_at) VALUES(?,'TRIP_REWARD',?,?,?,?)",
                userId,tripId,points,after,now());
    }
    public record Summary(long balance,long totalEarned,BigDecimal savedCarbonKg,long tripCount) { }
    public Summary summary(long userId) {
        return jdbc.query("""
            SELECT a.balance,
              (SELECT COALESCE(SUM(t.earned_points),0) FROM trip_record t WHERE t.user_id=a.user_id) AS total_earned,
              (SELECT COALESCE(SUM(t.saved_carbon_kg),0) FROM trip_record t WHERE t.user_id=a.user_id) AS saved_carbon,
              (SELECT COUNT(*) FROM trip_record t WHERE t.user_id=a.user_id) AS trip_count
            FROM point_account a WHERE a.user_id=?
            """,(rs,n) -> new Summary(rs.getLong("balance"),rs.getLong("total_earned"),rs.getBigDecimal("saved_carbon"),rs.getLong("trip_count")),userId)
                .stream().findFirst().orElseThrow(()->BusinessException.notFound("积分账户不存在"));
    }
    public record Ledger(long id,String bizType,long bizId,long delta,long balanceAfter,OffsetDateTime createdAt) { }
    public PageResult<Ledger> ledger(long userId,int page,int size) {
        PageResult.validate(page,size);
        List<Ledger> rows=jdbc.query("SELECT * FROM point_ledger WHERE user_id=? ORDER BY created_at DESC,id DESC LIMIT ? OFFSET ?",
                (rs,n) -> new Ledger(rs.getLong("id"),rs.getString("biz_type"),rs.getLong("biz_id"),rs.getLong("delta"),
                        rs.getLong("balance_after"),rs.getObject("created_at",LocalDateTime.class).atOffset(ZoneOffset.UTC)),userId,size,(long)page*size);
        long total=jdbc.queryForObject("SELECT COUNT(*) FROM point_ledger WHERE user_id=?",Long.class,userId);
        return new PageResult<>(rows,page,size,total);
    }
    private LocalDateTime now() { return LocalDateTime.ofInstant(clock.instant(),ZoneOffset.UTC); }
}

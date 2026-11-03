package edu.green.trip;

import edu.green.common.BusinessException;
import edu.green.common.PageResult;
import edu.green.points.PointWalletService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.UUID;

@Service
public class TripService {
    private final JdbcTripRepository trips;
    private final CarbonCalculator calculator;
    private final PointWalletService wallets;
    private final ApplicationEventPublisher events;
    private final Clock clock;
    public TripService(JdbcTripRepository trips,CarbonCalculator calculator,PointWalletService wallets,ApplicationEventPublisher events,Clock clock) {
        this.trips=trips;this.calculator=calculator;this.wallets=wallets;this.events=events;this.clock=clock;
    }
    public record ReportResult(boolean duplicate,TripView trip) { }
    @Transactional(rollbackFor=Exception.class)
    public ReportResult report(long userId,ReportTripRequest request) {
        if(request.completedAt().toInstant().isAfter(clock.instant())) throw BusinessException.badRequest("行程完成时间不能晚于服务器当前时间");
        String key=request.clientTripId().toLowerCase(Locale.ROOT);
        BigDecimal distance=request.distanceKm().setScale(2);
        LocalDateTime completed=LocalDateTime.ofInstant(request.completedAt().toInstant().truncatedTo(ChronoUnit.MICROS),ZoneOffset.UTC);
        // 同一个用户的发分按账户行锁串行执行；幂等查询使用当前锁定读。
        wallets.lockAccount(userId);
        var prior=trips.findRequestForUpdate(userId,key);
        if(prior.isPresent()) {
            var old=prior.get();
            if(old.mode()!=request.mode() || old.distance().compareTo(distance)!=0 || !old.completedAt().equals(completed)) {
                throw BusinessException.conflict("同一行程编号的内容与原记录不同，请核对后提交");
            }
            return new ReportResult(true,trips.findView(userId,old.id()).orElseThrow());
        }
        CarbonRule rule=trips.currentRule();
        CarbonCalculator.Result calculated=calculator.calculate(distance,request.mode(),rule);
        long id=trips.insert(userId,key,request.mode(),distance,completed,rule,calculated,LocalDateTime.ofInstant(clock.instant(),ZoneOffset.UTC));
        // 默认同步事件监听器加入当前事务，失败异常向外传播，行程、账户和流水共同回滚。
        events.publishEvent(new TripAccepted(UUID.randomUUID().toString(),id,userId,calculated.earnedPoints(),rule.versionName()));
        return new ReportResult(false,trips.findView(userId,id).orElseThrow());
    }
    @Transactional(readOnly=true)
    public PageResult<TripView> list(long userId,int page,int size) {
        PageResult.validate(page,size);return new PageResult<>(trips.list(userId,page,size),page,size,trips.count(userId));
    }
    public TripView detail(long userId,long tripId) {
        return trips.findView(userId,tripId).orElseThrow(()->BusinessException.notFound("行程不存在"));
    }
}

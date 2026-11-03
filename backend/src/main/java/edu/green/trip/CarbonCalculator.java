package edu.green.trip;

import edu.green.common.BusinessException;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class CarbonCalculator {
    public record Result(BigDecimal savedCarbonKg,long earnedPoints) { }
    public Result calculate(BigDecimal distance,TransportMode mode,CarbonRule rule) {
        if(distance==null || distance.scale()>2 || distance.compareTo(new BigDecimal("0.01"))<0
                || distance.compareTo(new BigDecimal("100.00"))>0 || mode==null) {
            throw BusinessException.badRequest("里程应为 0.01 至 100.00 公里，最多两位小数，且交通方式必填");
        }
        // 先用完整精度计算积分，最后才确定存储精度，避免页面显示取整影响实际记账。
        BigDecimal saved=distance.multiply(rule.carFactor().subtract(rule.factor(mode))).max(BigDecimal.ZERO);
        long points=saved.multiply(BigDecimal.valueOf(rule.pointsPerKg())).setScale(0,RoundingMode.FLOOR).longValueExact();
        return new Result(saved.setScale(6,RoundingMode.UNNECESSARY),points);
    }
}

package edu.green.trip;

import java.math.BigDecimal;

public record CarbonRule(long id,String versionName,BigDecimal carFactor,BigDecimal busFactor,
                         BigDecimal metroFactor,BigDecimal bikeFactor,int pointsPerKg) {
    public BigDecimal factor(TransportMode mode) {
        return switch(mode) { case BUS -> busFactor; case METRO -> metroFactor; case BIKE -> bikeFactor; };
    }
}

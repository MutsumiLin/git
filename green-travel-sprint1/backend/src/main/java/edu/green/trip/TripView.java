package edu.green.trip;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TripView(long id,String clientTripId,TransportMode mode,BigDecimal distanceKm,
                       OffsetDateTime completedAt,String source,String ruleVersion,BigDecimal savedCarbonKg,
                       long earnedPoints,long balanceAfter,OffsetDateTime createdAt) { }

package edu.green.trip;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ReportTripRequest(
        @NotBlank @Pattern(regexp="[a-zA-Z0-9][a-zA-Z0-9_-]{7,63}",message="编号为 8 至 64 位字母数字下划线或连字符") String clientTripId,
        @NotNull TransportMode mode,
        @NotNull @DecimalMin("0.01") @DecimalMax("100.00") @Digits(integer=3,fraction=2) BigDecimal distanceKm,
        @NotNull OffsetDateTime completedAt) { }

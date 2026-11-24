package edu.green.trip;
import edu.green.common.*;
import edu.green.identity.DomainPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.*;

@RestController
@RequestMapping("/api/admin/carbon-rules")
public class RuleController {
 private final JdbcTemplate jdbc;private final AuditService audit;private final Clock clock;
 public RuleController(JdbcTemplate j,AuditService a,Clock c){jdbc=j;audit=a;clock=c;}
 public record Input(@NotNull @Pattern(regexp="[a-zA-Z0-9_-]{3,40}") String versionName,
  @NotNull @DecimalMin("0.0") @DecimalMax("10.0") @Digits(integer=2,fraction=6) BigDecimal carFactor,
  @NotNull @DecimalMin("0.0") @DecimalMax("10.0") @Digits(integer=2,fraction=6) BigDecimal busFactor,
  @NotNull @DecimalMin("0.0") @DecimalMax("10.0") @Digits(integer=2,fraction=6) BigDecimal metroFactor,
  @NotNull @DecimalMin("0.0") @DecimalMax("10.0") @Digits(integer=2,fraction=6) BigDecimal bikeFactor,
  @Min(1) @Max(10000) int pointsPerKg){}
 @GetMapping public ApiResponse<?> list(){return ApiResponse.ok(jdbc.queryForList("SELECT * FROM carbon_rule_version ORDER BY id DESC"));}
 @PostMapping @Transactional
 public ApiResponse<?> publish(@AuthenticationPrincipal DomainPrincipal actor,@Valid @RequestBody Input in){
  if(jdbc.queryForObject("SELECT COUNT(*) FROM carbon_rule_version WHERE version_name=?",Long.class,in.versionName())>0)throw BusinessException.conflict("版本名称已存在");
  jdbc.update("INSERT INTO carbon_rule_version(version_name,car_factor,bus_factor,metro_factor,bike_factor,points_per_kg,published_at) VALUES(?,?,?,?,?,?,?)",
   in.versionName(),in.carFactor(),in.busFactor(),in.metroFactor(),in.bikeFactor(),in.pointsPerKg(),LocalDateTime.ofInstant(clock.instant(),ZoneOffset.UTC));
  audit.record(actor.id(),"RULE_PUBLISH",in.versionName());return ApiResponse.ok(in);
 }
}

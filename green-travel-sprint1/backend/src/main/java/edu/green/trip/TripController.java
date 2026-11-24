package edu.green.trip;

import edu.green.common.ApiResponse;
import edu.green.common.PageResult;
import edu.green.identity.DomainPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
public class TripController {
    private final TripService trips;
    private final JdbcTripRepository repository;
    public TripController(TripService trips,JdbcTripRepository repository) {this.trips=trips;this.repository=repository;}
    @PostMapping("/api/trips")
    ResponseEntity<ApiResponse<TripService.ReportResult>> report(@AuthenticationPrincipal DomainPrincipal user,@Valid @RequestBody ReportTripRequest request) {
        var result=trips.report(user.id(),request);return ResponseEntity.status(result.duplicate()?200:201).body(ApiResponse.ok(result));
    }
    @GetMapping("/api/me/trips")
    ApiResponse<PageResult<TripView>> list(@AuthenticationPrincipal DomainPrincipal user,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {
        return ApiResponse.ok(trips.list(user.id(),page,size));
    }
    @GetMapping("/api/me/trips/{id}")
    ApiResponse<TripView> detail(@AuthenticationPrincipal DomainPrincipal user,@PathVariable long id) {return ApiResponse.ok(trips.detail(user.id(),id));}
    @GetMapping("/api/carbon-rules/current")
    ApiResponse<Map<String,Object>> rule() {return ApiResponse.ok(Map.of("rule",repository.currentRule(),"simulation",true,"unit","kgCO2e/人公里","distanceMin","0.01","distanceMax","100.00"));}
}

package edu.green.mall;
import edu.green.common.ApiResponse;
import edu.green.identity.DomainPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController
public class RedemptionController {
 private final RedemptionService service;public RedemptionController(RedemptionService s){service=s;}
 @PostMapping("/api/me/redemptions") public ResponseEntity<?> create(@AuthenticationPrincipal DomainPrincipal user,@Valid @RequestBody RedemptionService.Input in){var result=service.create(user.id(),in);return ResponseEntity.status(result.duplicate()?200:201).body(ApiResponse.ok(result));}
 @GetMapping("/api/me/redemptions") public ApiResponse<?> list(@AuthenticationPrincipal DomainPrincipal user,@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="10")int size){return ApiResponse.ok(service.list(user.id(),page,size));}
 @GetMapping("/api/me/redemptions/{id}") public ApiResponse<?> detail(@AuthenticationPrincipal DomainPrincipal user,@PathVariable long id){return ApiResponse.ok(service.owned(user.id(),id));}
 @PostMapping("/api/me/redemptions/{id}/cancel") public ApiResponse<?> cancel(@AuthenticationPrincipal DomainPrincipal user,@PathVariable long id){return ApiResponse.ok(service.cancel(user.id(),id));}
 @GetMapping("/api/merchant/redemptions") public ApiResponse<?> merchant(@AuthenticationPrincipal DomainPrincipal user){return ApiResponse.ok(service.merchantOrders(user.merchantId()));}
 public record Verify(@NotNull @Pattern(regexp="[a-f0-9-]{36}")String verifyCode){}
 @PostMapping("/api/merchant/redemptions/verify") public ApiResponse<?> verify(@AuthenticationPrincipal DomainPrincipal user,@Valid @RequestBody Verify in){return ApiResponse.ok(service.verify(user.id(),user.merchantId(),in.verifyCode()));}
}

package edu.green.points;

import edu.green.common.ApiResponse;
import edu.green.common.PageResult;
import edu.green.identity.DomainPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class PointController {
    private final PointWalletService wallets;
    public PointController(PointWalletService wallets) { this.wallets=wallets; }
    @GetMapping("/api/me/points")
    ApiResponse<PointWalletService.Summary> summary(@AuthenticationPrincipal DomainPrincipal user) {
        return ApiResponse.ok(wallets.summary(user.id()));
    }
    @GetMapping("/api/me/point-ledger")
    ApiResponse<PageResult<PointWalletService.Ledger>> ledger(@AuthenticationPrincipal DomainPrincipal user,
            @RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {
        return ApiResponse.ok(wallets.ledger(user.id(),page,size));
    }
}

package edu.green.identity;

import edu.green.common.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class AuthController {
    @GetMapping("/api/auth/csrf")
    ApiResponse<Map<String,String>> csrf(CsrfToken token) {
        return ApiResponse.ok(Map.of("headerName",token.getHeaderName(),"token",token.getToken()));
    }
    @GetMapping("/api/me")
    ApiResponse<CurrentUser> me(@AuthenticationPrincipal DomainPrincipal principal) { return ApiResponse.ok(CurrentUser.from(principal)); }
    @GetMapping("/api/health")
    ApiResponse<Map<String,String>> health() { return ApiResponse.ok(Map.of("status","UP","iteration","Sprint 1")); }
}

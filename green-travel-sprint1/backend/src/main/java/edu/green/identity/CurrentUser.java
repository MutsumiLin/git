package edu.green.identity;

public record CurrentUser(long id, String username, String role, Long merchantId) {
    public static CurrentUser from(DomainPrincipal principal) {
        return new CurrentUser(principal.id(),principal.getUsername(),principal.role(),principal.merchantId());
    }
}

package edu.green.identity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

public final class DomainPrincipal implements UserDetails {
    private final UserAccount user;
    public DomainPrincipal(UserAccount user) { this.user=user; }
    public long id() { return user.id(); }
    public String role() { return user.role(); }
    public Long merchantId() { return user.merchantId(); }
    @Override public String getUsername() { return user.username(); }
    @Override public String getPassword() { return user.passwordHash(); }
    @Override public boolean isEnabled() { return user.enabled(); }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_"+user.role()));
    }
}

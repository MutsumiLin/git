package edu.green.identity;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class UserProvisioningService {
    private final JdbcUserRepository users;
    private final PasswordEncoder passwords;
    private final ApplicationEventPublisher events;
    private final Clock clock;
    public UserProvisioningService(JdbcUserRepository users,PasswordEncoder passwords,ApplicationEventPublisher events,Clock clock) {
        this.users=users;this.passwords=passwords;this.events=events;this.clock=clock;
    }
    @Transactional(rollbackFor=Exception.class)
    public UserAccount createIfAbsent(String username,String password,String role,Long merchantId) {
        var existing=users.findByUsername(username);
        if(existing.isPresent()) return existing.get();
        long id=users.insert(username,passwords.encode(password),role,merchantId,LocalDateTime.ofInstant(clock.instant(),ZoneOffset.UTC));
        events.publishEvent(new UserProvisioned(id,role));
        return users.findByUsername(username).orElseThrow();
    }
}

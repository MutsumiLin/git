package edu.green.common;
import java.time.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
@Service
public class AuditService {
 private final JdbcTemplate jdbc; private final Clock clock;
 public AuditService(JdbcTemplate jdbc,Clock clock){this.jdbc=jdbc;this.clock=clock;}
 @Transactional(propagation=Propagation.MANDATORY)
 public void record(long actor,String action,String target){
  jdbc.update("INSERT INTO audit_log(actor_id,action,target,created_at) VALUES(?,?,?,?)",actor,action,target,LocalDateTime.ofInstant(clock.instant(),ZoneOffset.UTC));
 }
}

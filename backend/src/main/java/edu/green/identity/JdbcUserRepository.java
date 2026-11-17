package edu.green.identity;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class JdbcUserRepository {
    private final JdbcTemplate jdbc;
    public JdbcUserRepository(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    public Optional<UserAccount> findByUsername(String name) {
        return jdbc.query("SELECT id,username,password_hash,role,merchant_id,enabled FROM sys_user WHERE username=?",
                (rs,n) -> new UserAccount(rs.getLong("id"),rs.getString("username"),rs.getString("password_hash"),
                        rs.getString("role"),rs.getObject("merchant_id",Long.class),rs.getBoolean("enabled")),name).stream().findFirst();
    }
    public long insert(String username,String hash,String role,Long merchantId,LocalDateTime now) {
        GeneratedKeyHolder keys=new GeneratedKeyHolder();
        jdbc.update(connection -> {
            var ps=connection.prepareStatement("INSERT INTO sys_user(username,password_hash,role,merchant_id,enabled,created_at) VALUES(?,?,?,?,TRUE,?)",Statement.RETURN_GENERATED_KEYS);
            ps.setString(1,username);ps.setString(2,hash);ps.setString(3,role);ps.setObject(4,merchantId);ps.setObject(5,now);
            return ps;
        },keys);
        return keys.getKey().longValue();
    }
}

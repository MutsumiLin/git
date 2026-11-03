package edu.green;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.green.identity.JdbcUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class TestSupport {
    @Autowired protected MockMvc mvc;
    @Autowired protected ObjectMapper json;
    @Autowired protected JdbcTemplate jdbc;
    @Autowired protected JdbcUserRepository users;
    @BeforeEach
    void resetBusinessData() throws Exception {
        try(var connection=jdbc.getDataSource().getConnection()) {
            String url=connection.getMetaData().getURL();
            if(url.startsWith("jdbc:mysql:")) {
                String database=connection.getCatalog();
                if(database==null || !database.endsWith("_test"))throw new IllegalStateException("MySQL 自动化测试必须使用名称以 _test 结尾的独立测试库");
            }
        }
        jdbc.update("DELETE FROM point_ledger");
        jdbc.update("DELETE FROM trip_record");
        jdbc.update("UPDATE point_account SET balance=0");
        jdbc.update("DELETE FROM carbon_rule_version WHERE version_name<>'simulation-v1'");
    }
    protected record Session(MockHttpSession http,String header,String token) { }
    protected Session anonymous() throws Exception {
        MvcResult result=mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andReturn();
        JsonNode data=json.readTree(result.getResponse().getContentAsString()).path("data");
        return new Session((MockHttpSession)result.getRequest().getSession(),data.path("headerName").asText(),data.path("token").asText());
    }
    protected Session login(String username) throws Exception {
        Session before=anonymous();
        MvcResult result=mvc.perform(post("/api/auth/login").session(before.http()).header(before.header(),before.token())
                .contentType("application/x-www-form-urlencoded").param("username",username).param("password","GreenDemo!2026"))
                .andExpect(status().isOk()).andReturn();
        MockHttpSession http=(MockHttpSession)result.getRequest().getSession();
        MvcResult csrf=mvc.perform(get("/api/auth/csrf").session(http)).andExpect(status().isOk()).andReturn();
        JsonNode data=json.readTree(csrf.getResponse().getContentAsString()).path("data");
        return new Session(http,data.path("headerName").asText(),data.path("token").asText());
    }
    protected Map<String,Object> trip(String key,String mode,String distance) {
        return Map.of("clientTripId",key,"mode",mode,"distanceKm",distance,
                "completedAt",Instant.now().minusSeconds(300).truncatedTo(ChronoUnit.MILLIS).toString());
    }
    protected Map<String,Object> trip() { return trip(UUID.randomUUID().toString(),"BUS","10.00"); }
    protected MvcResult report(Session session,Object body,int expected) throws Exception {
        return mvc.perform(post("/api/trips").session(session.http()).header(session.header(),session.token())
                .contentType("application/json").content(json.writeValueAsBytes(body))).andExpect(status().is(expected)).andReturn();
    }
    protected long userId(String username) { return users.findByUsername(username).orElseThrow().id(); }
    protected long balance(String username) { return jdbc.queryForObject("SELECT balance FROM point_account WHERE user_id=?",Long.class,userId(username)); }
}

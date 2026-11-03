package edu.green.identity;

import edu.green.TestSupport;
import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthFlowTest extends TestSupport {
    @Test void unauthenticatedAccessIs401() throws Exception {
        mvc.perform(get("/api/me")).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }
    @Test void publicHealthAndCsrfWork() throws Exception {
        mvc.perform(get("/api/health")).andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("UP"));
        mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andExpect(jsonPath("$.data.token").isNotEmpty());
    }
    @Test void loginWithoutCsrfIsRejected() throws Exception {
        mvc.perform(post("/api/auth/login").param("username","citizen").param("password","GreenDemo!2026"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("CSRF_INVALID"));
    }
    @Test void wrongPasswordIs401() throws Exception {
        var session=anonymous();
        mvc.perform(post("/api/auth/login").session(session.http()).header(session.header(),session.token())
                .param("username","citizen").param("password","wrong"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("BAD_CREDENTIALS"));
    }
    @Test void currentIdentityDoesNotExposePassword() throws Exception {
        var session=login("citizen");
        mvc.perform(get("/api/me").session(session.http())).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("citizen")).andExpect(jsonPath("$.data.role").value("USER"))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist()).andExpect(jsonPath("$.data.password").doesNotExist());
    }
    @Test void roleBoundariesAndLogoutWork() throws Exception {
        var citizen=login("citizen");
        mvc.perform(get("/api/admin/products").session(citizen.http())).andExpect(status().isForbidden());
        var admin=login("operator");
        mvc.perform(get("/api/admin/products").session(admin.http())).andExpect(status().isOk());
        mvc.perform(get("/api/me/points").session(admin.http())).andExpect(status().isForbidden());
        mvc.perform(post("/api/auth/logout").session(citizen.http()).header(citizen.header(),citizen.token())).andExpect(status().isOk());
        mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
    }
}

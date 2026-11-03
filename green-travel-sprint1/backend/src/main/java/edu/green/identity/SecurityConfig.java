package edu.green.identity;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.green.common.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import java.io.IOException;

@Configuration
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean UserDetailsService userDetailsService(JdbcUserRepository users) {
        return name -> new DomainPrincipal(users.findByUsername(name).orElseThrow(() -> new UsernameNotFoundException("账号或密码不正确")));
    }
    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http,ObjectMapper json) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/health","/api/auth/csrf","/api/auth/login").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/merchant/**").hasRole("MERCHANT")
                .requestMatchers("/api/me/**","/api/trips","/api/products/**").hasRole("USER")
                .anyRequest().authenticated());
        // 同源 Vue 客户端先 GET token，再将明文 token 放入指定请求头。
        http.csrf(csrf -> csrf.csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()));
        http.requestCache(cache -> cache.disable());
        http.exceptionHandling(errors -> errors
                .authenticationEntryPoint((req,res,ex) -> write(json,res,401,ApiResponse.error("UNAUTHENTICATED","请先登录")))
                .accessDeniedHandler((req,res,ex) -> write(json,res,403,ApiResponse.error(ex instanceof CsrfException ? "CSRF_INVALID":"FORBIDDEN",
                        ex instanceof CsrfException ? "会话校验失效，请刷新后重试":"当前角色没有此操作权限"))));
        http.formLogin(form -> form.loginProcessingUrl("/api/auth/login")
                .successHandler((req,res,auth) -> write(json,res,200,ApiResponse.ok(CurrentUser.from((DomainPrincipal)auth.getPrincipal()))))
                .failureHandler((req,res,ex) -> write(json,res,401,ApiResponse.error("BAD_CREDENTIALS","账号或密码不正确"))));
        http.logout(logout -> logout.logoutUrl("/api/auth/logout").invalidateHttpSession(true).deleteCookies("JSESSIONID")
                .logoutSuccessHandler((req,res,auth) -> write(json,res,200,ApiResponse.ok(null))));
        return http.build();
    }
    private static void write(ObjectMapper json,HttpServletResponse response,int status,Object body) throws IOException {
        response.setStatus(status);response.setContentType("application/json;charset=UTF-8");json.writeValue(response.getOutputStream(),body);
    }
}

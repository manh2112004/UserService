package org.User.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
@Configuration // Báo cho Spring biết đây là lớp cấu hình
@EnableWebSecurity // Kích hoạt tính năng bảo mật web
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Mở chính xác endpoint đăng ký
                        .requestMatchers("/api/v1/auth/register",
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/logout").permitAll()
                        .requestMatchers("/public/**").permitAll()
                        // Các request còn lại mới cần xác thực
                        .anyRequest().authenticated()
                )
                // Cấu hình Resource Server chỉ áp dụng cho các request cần bảo mật
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> {})
                );

        return http.build();
    }
}

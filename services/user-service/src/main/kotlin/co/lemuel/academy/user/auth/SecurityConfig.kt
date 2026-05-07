package co.lemuel.academy.user.auth

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/api/users/signup", "/api/users/login",
                        "/api/users/oauth/**", "/actuator/**"
                    ).permitAll()
                    .anyRequest().authenticated()
            }
            // 운영 시 JwtAuthFilter 추가 (api-gateway 가 1차 검증, 서비스가 2차)
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
        return http.build()
    }
}

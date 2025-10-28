package config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration that permits anonymous access to authentication endpoints
 * so clients can register and login without HTTP Basic blocking them.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeRequests()
                .antMatchers("/", "/login", "/register", "/booking", "/rating", "/timer", "/api/auth/**", "/h2-console/**").permitAll()
                .anyRequest().authenticated()
            .and()
                .httpBasic();

        http.headers().frameOptions().disable(); // allow H2 console frames

        return http.build();
    }
}

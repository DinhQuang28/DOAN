package quanly.com.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/images/**", "/webjars/**", "/api/public/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/reports/**", "/services/**", "/room-types/**", "/price-seasons/**", "/promotions/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/rooms/**", "/bookings/**", "/guests/**", "/payments/**", "/facilities/**").hasAnyRole("ADMIN", "MANAGER", "RECEPTIONIST")
                .requestMatchers("/dashboard/**", "/dashboard").hasAnyRole("ADMIN", "MANAGER", "RECEPTIONIST")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .rememberMe(remember -> remember
                .key("sunshine-secret-key")
                .tokenValiditySeconds(86400)
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/ws/**")
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

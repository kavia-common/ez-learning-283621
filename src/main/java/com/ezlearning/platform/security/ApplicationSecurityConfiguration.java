package com.ezlearning.platform.security;

import com.ezlearning.platform.auth.EzLearningUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.authentication.AuthenticationConfiguration;

/**
 * Application security configuration for Spring Security 6 / Spring Boot 3.
 * - Configures form login/logout
 * - Permits static resources and public pages
 * - Disables CSRF and frame options for H2 console in dev environments
 * - Sets up DaoAuthenticationProvider with a custom UserDetailsService and role mapping.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class ApplicationSecurityConfiguration {

    private final EzLearningUserDetailsService userDetailsService;

    public ApplicationSecurityConfiguration(EzLearningUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // PUBLIC_INTERFACE
    /**
     * BCrypt password encoder used for hashing user passwords.
     * @return PasswordEncoder instance with strength 11
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(11);
    }

    // PUBLIC_INTERFACE
    /**
     * Maps granted authorities by:
     * - Upper-casing values
     * - Applying default authority "USER" when none
     * - Using the default "ROLE_" prefix so "USER" becomes "ROLE_USER".
     *
     * @return GrantedAuthoritiesMapper for role normalization
     */
    @Bean
    public GrantedAuthoritiesMapper authoritiesMapper() {
        SimpleAuthorityMapper mapper = new SimpleAuthorityMapper();
        mapper.setConvertToUpperCase(true);
        mapper.setDefaultAuthority("USER");
        return mapper;
    }

    // PUBLIC_INTERFACE
    /**
     * Configures DAO-based authentication to use our UserDetailsService, password encoder, and authorities mapper.
     * @param encoder injected PasswordEncoder
     * @param mapper injected GrantedAuthoritiesMapper
     * @return configured DaoAuthenticationProvider
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(PasswordEncoder encoder, GrantedAuthoritiesMapper mapper) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(encoder);
        provider.setAuthoritiesMapper(mapper);
        return provider;
    }

    // PUBLIC_INTERFACE
    /**
     * Primary security filter chain configuration.
     * - Public endpoints: "/", "/index", "/discover", "/cursos", "/api/**", "/register", and static assets
     * - Login page at "/login"
     * - Logout via "/logout" with redirect to "/logout-success"
     * - H2 console is permitted with CSRF and frame options disabled to work properly in dev
     *
     * @param http HttpSecurity builder
     * @return built SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Allow H2 console and simplify CSRF for this project context
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/", "/index", "/discover", "/cursos",
                        "/api/**",
                        "/register",
                        "/h2-console/**",
                        "/css/**", "/js/**", "/img/**", "/favicon.ico"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/logout-success")
                .clearAuthentication(true)
                .invalidateHttpSession(true)
                .permitAll()
            );

        // Use DAO authentication provider
        http.authenticationProvider(authenticationProvider(passwordEncoder(), authoritiesMapper()));
        return http.build();
    }

    // PUBLIC_INTERFACE
    /**
     * Expose AuthenticationManager for authentication flows where needed.
     * @param configuration Authentication configuration provided by Spring
     * @return AuthenticationManager
     * @throws Exception when the manager can't be built
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}

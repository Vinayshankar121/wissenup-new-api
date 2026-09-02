package com.wissenup.shared.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import java.util.List;

/**
 * Spring Security configuration for JWT-based authentication and multi-tenant access control.
 *
 * Sets up:
 * - JWT token validation filter
 * - CORS configuration
 * - Stateless session management
 * - Security headers
 * - AOP aspect for tenant filtering
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class JwtSecurityConfiguration {

    @Value("${JWT_SECRET}")
    private String jwtSecret;

    @Value("${JWT_EXPIRY_MINUTES:60}")
    private long jwtExpiryMinutes;

    /**
     * Create password encoder bean for user password hashing.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Create JWT service bean for token generation and validation.
     */
    @Bean
    public JwtService jwtService() {
        return new JwtServiceImpl(jwtSecret, jwtExpiryMinutes);
    }

    /**
     * Create tenant repository aspect for AOP-based tenant filtering.
     */
    @Bean
    public TenantRepositoryAspect tenantRepositoryAspect() {
        return new TenantRepositoryAspect();
    }

    /**
     * Create CORS configuration source.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
        @Value("${spring.mvc.cors.allowed-origins}") String allowedOrigins) {

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("X-Total-Count", "X-Page-Number", "X-Page-Size"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * Configure Spring Security filter chain.
     * - Disable CSRF (stateless JWT authentication)
     * - Use stateless session management
     * - Add JWT filter before authentication filter
     * - Configure CORS
     * - Require authentication on protected endpoints
     */
    @Bean
    public SecurityFilterChain filterChain(
        HttpSecurity http,
        JwtService jwtService,
        CorsConfigurationSource corsConfigurationSource)
        throws Exception {

        JwtAuthenticationFilter jwtAuthenticationFilter =
            new JwtAuthenticationFilter(jwtService);

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler())
            )
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/api/v1/auth/login", "/api/v1/auth/otp/verify", "/api/v1/auth/otp/resend").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/subscription-plans/**").permitAll()
                .requestMatchers("/api/v1/health/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/health", "/actuator/health/live", "/actuator/health/ready").permitAll()
                // Protected endpoints
                .anyRequest().authenticated()
            )
            // Authenticate bearer tokens after the security context exists and
            // immediately before Spring performs endpoint authorization.
            .addFilterBefore(jwtAuthenticationFilter, AuthorizationFilter.class)
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.deny())
                .xssProtection(xss -> {})
            );

        return http.build();
    }

    private AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, exception) -> {
            response.setStatus(401);
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"success\":false,\"message\":\"Authentication is required\"}"
            );
        };
    }

    private AccessDeniedHandler accessDeniedHandler() {
        return (request, response, exception) -> {
            response.setStatus(403);
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"success\":false,\"message\":\"Authenticated user is not allowed to perform this operation\"}"
            );
        };
    }

    /**
     * WebMvcConfigurer for additional web configuration.
     * (Currently empty but can be extended if needed)
     */
    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            // Can add interceptors, formatters, etc. here if needed
        };
    }
}

package com.Tome.tome.global.config;

import com.Tome.tome.domain.auth.handler.LoginFailureHandler;
import com.Tome.tome.domain.auth.handler.LoginSuccessHandler;
import com.Tome.tome.domain.auth.handler.RefreshTokenLogoutHandler;
import com.Tome.tome.global.config.jwt.TokenAuthenticationFilter;
import com.Tome.tome.global.config.jwt.TokenProvider;
import com.Tome.tome.global.util.JsonResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final TokenProvider tokenProvider;
    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;
    private final RefreshTokenLogoutHandler refreshTokenLogoutHandler;

    @Value("${cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Value("${cookie.refresh-token.secure}")
    private boolean cookieSecure;

    @Value("${cookie.refresh-token.same-site}")
    private String cookieSameSite;

    @Bean
    public WebSecurityCustomizer configure(){
        return (web) -> web.ignoring().requestMatchers("/static/**");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfTokenRepository.setCookieCustomizer(cookie -> cookie
                .secure(cookieSecure)
                .sameSite(cookieSameSite));

        return http
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/login","/signup","/user","/error","/api/token","/api/csrf",
                                "/swagger-ui/**","/swagger-ui.html","/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated())
                .formLogin(formLogin -> formLogin
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .successHandler(loginSuccessHandler)
                        .failureHandler(loginFailureHandler))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .addLogoutHandler(refreshTokenLogoutHandler)
                        .logoutSuccessHandler((request, response, authentication) ->
                                JsonResponseUtil.write(response, 200, Map.of("message", "로그아웃되었습니다"))))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, e) ->
                                JsonResponseUtil.write(response, 401, Map.of("message", "로그인이 필요합니다")))
                        .accessDeniedHandler((request, response, e) ->
                                JsonResponseUtil.write(response, 403, Map.of("message",
                                        e instanceof CsrfException ? "CSRF 토큰이 없거나 올바르지 않습니다" : "권한이 없습니다"))))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers("/api/token")
                        .ignoringRequestMatchers(request -> request.getHeader("Authorization") != null))
                .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
                .addFilterBefore(new TokenAuthenticationFilter(tokenProvider), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // 프론트엔드(다른 포트/도메인)에서 쿠키를 포함해 호출할 수 있게 허용
    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-XSRF-TOKEN"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}

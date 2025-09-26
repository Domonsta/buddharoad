// src/main/java/com/buddharoad/config/SecurityConfig.java
package com.buddharoad.config;

import com.buddharoad.security.JwtAccessDeniedHandler;
import com.buddharoad.security.JwtAuthenticationEntryPoint;
import com.buddharoad.security.JwtAuthenticationFilter;
import com.buddharoad.security.JwtTokenProvider;
import com.buddharoad.service.Member.MemberDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // HttpMethod 임포트
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity(debug = true) // debug = true는 보안 필터 체인 디버깅에 유용
@EnableMethodSecurity(prePostEnabled = true) // @PreAuthorize, @PostAuthorize 등 어노테이션 기반 보안 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final MemberDetailsService memberDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(memberDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(authenticationProvider);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler))
                .authorizeHttpRequests(authorize -> authorize
                        // ⭐⭐ 1. 로그인, 회원가입 등 인증이 필요 없는 API는 가장 먼저 permitAll() ⭐⭐
                        .requestMatchers(
                                HttpMethod.POST, "/api/auth/login", "/api/auth/signup" // 로그인과 회원가입은 POST 메서드
                        ).permitAll()
                        .requestMatchers(
                                "/api/auth/check-login-id", "/api/auth/check-email", "/api/auth/check-username", // 중복확인은 GET
                                "/api/temples/**",          // 사찰 정보 조회 (비회원 가능)
                                "/error",                   // 스프링 기본 에러 페이지
                                "/uploaded/**"              // 업로드된 파일 접근 (이미지 등)
                        ).permitAll()

                        // ⭐⭐ 2. 관리자 페이지 관련 API는 먼저 정의하여 충돌 방지 ⭐⭐
                        // /api/auth/admin/me 등 구체적인 경로를 먼저 정의
                        .requestMatchers("/api/auth/admin/me").hasAnyRole("SYSTEM_ADMIN", "CONTENT_ADMIN") // 관리자 본인 정보 조회/수정
                        .requestMatchers("/api/auth/admin/members/**").hasAnyRole("SYSTEM_ADMIN", "CONTENT_ADMIN") // 회원 관리
                        .requestMatchers("/api/auth/admin/system/**").hasRole("SYSTEM_ADMIN")
                        .requestMatchers("/api/auth/admin/content/**").hasRole("CONTENT_ADMIN")
                        .requestMatchers("/api/auth/admin/dashboard").hasAnyRole("SYSTEM_ADMIN", "CONTENT_ADMIN")

                        // ⭐⭐ 3. 일반 사용자(USER) 및 관리자(CONTENT_ADMIN, SYSTEM_ADMIN) 공통으로 접근 가능한 API
                        .requestMatchers("/api/auth/me/**").hasAnyRole("USER", "CONTENT_ADMIN", "SYSTEM_ADMIN") // 마이페이지 (내 정보, 내 리뷰/댓글 등)
                        .requestMatchers("/api/reviews/**").hasAnyRole("USER", "CONTENT_ADMIN", "SYSTEM_ADMIN") // 리뷰 관련
                        .requestMatchers("/api/temples/*/comments/**").hasAnyRole("USER", "CONTENT_ADMIN", "SYSTEM_ADMIN") // 사찰 댓글 관련

                        // 4. 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                // JWT 필터 추가: UsernamePasswordAuthenticationFilter 이전에 실행되도록 설정
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
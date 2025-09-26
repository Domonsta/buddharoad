package com.buddharoad.security;

import com.buddharoad.service.Member.MemberDetailsService; // ✨ 이 임포트 추가!
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor; // ✨ RequiredArgsConstructor 추가!
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
// import org.springframework.security.core.userdetails.User; // ❌ 이 임포트는 더 이상 필요 없어!
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor // ✨ 이 어노테이션 추가!
public class JwtTokenProvider {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.expiration-time}")
    private long expirationTime;

    private Key key;

    private final MemberDetailsService memberDetailsService; // ✨ MemberDetailsService 주입!

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // JWT 토큰 생성 (이 부분은 수정할 필요 없음)
    public String createToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date now = new Date();
        Date validity = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim("auth", authorities)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // JWT 토큰으로 인증 정보 조회 (⭐⭐⭐ 이 부분을 이렇게 수정해! ⭐⭐⭐)
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String username = claims.getSubject(); // 토큰의 subject (로그인 ID)

        // DB에서 Member 객체를 조회 (이 Member 객체는 올바른 role 정보를 가짐)
        UserDetails principal = memberDetailsService.loadUserByUsername(username);

        // 👇👇👇 이 부분을 이렇게 수정해야 해! 👇👇👇
        // Authentication 객체를 생성할 때, DB에서 로드한 principal (Member 객체)의 getAuthorities() 메서드를 사용해야 함
        // 이렇게 하면 토큰의 claims에 있는 auth 정보가 아닌, DB에 있는 최신/정확한 권한 정보를 사용하게 돼.
        return new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities());
    }

    // JWT 토큰 유효성 검사 (이 부분은 수정할 필요 없음)
    public boolean validateToken(String token) {
        // ... (기존 코드 유지) ...
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            System.out.println("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            System.out.println("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            System.out.println("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            System.out.println("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    // Request Header에서 토큰 정보 추출 (이 부분은 수정할 필요 없음)
    public String resolveToken(jakarta.servlet.http.HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
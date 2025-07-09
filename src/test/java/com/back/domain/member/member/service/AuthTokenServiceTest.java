package com.back.domain.member.member.service;

import com.back.domain.member.member.entity.Member;
import com.back.standard.util.Ut;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class AuthTokenServiceTest {
    @Autowired
    private MemberService memberService;
    @Autowired
    private AuthTokenService authTokenService;

    @Value("${custom.accessToken.expirationSeconds}")
    private int accessTokenExpirationSeconds;

    @Value("${custom.jwt.secretKey}")
    private String jwtSecretKey;

    @Test
    @DisplayName("authTokenService 서비스가 존재한다.")
    void t1() {
        assertThat(authTokenService).isNotNull();
    }

    @Test
    @DisplayName("jjwt 최신 방식으로 JWT 생성, {name=\"Paul\", age=23}")
    void t2() {
        long expireMillis = accessTokenExpirationSeconds * 1000L;
        byte[] keyBytes = jwtSecretKey.getBytes(StandardCharsets.UTF_8);
        SecretKey secretKey = Keys.hmacShaKeyFor(keyBytes);

        // 발행 시간과 만료 시간 설정
        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + expireMillis);

        Map<String, Object> payload = Map.of("name", "Paul", "age", 23);

        String jwt = Jwts.builder()
                .claims(payload) // 내용
                .issuedAt(issuedAt) // 생성날짜
                .expiration(expiration) // 만료날짜
                .signWith(secretKey) // 키 서명
                .compact();

        assertThat(jwt).isNotBlank();

        System.out.println("jwt = " + jwt);

        // 키가 유효한지 테스트
        Map<String, Object> parsedPayload = (Map<String, Object>) Jwts
                .parser()
                .verifyWith(secretKey)
                .build()
                .parse(jwt)
                .getPayload();

        assertThat(parsedPayload)
                .containsAllEntriesOf(payload);
    }

    @Test
    @DisplayName("Ut.jwt.toString 를 통해서 JWT 생성, {name=\"Paul\", age=23}")
    void t3() {
        Map<String, Object> payload = Map.of("name", "Paul", "age", 23);
        String jwt = Ut.jwt.toString(
                jwtSecretKey,
                accessTokenExpirationSeconds,
                payload
        );

        assertThat(jwt).isNotBlank();

        System.out.println("jwt = " + jwt);

        assertThat(Ut.jwt.isValid(jwtSecretKey, jwt)).isTrue();

        Map<String, Object> parsedPayload = Ut.jwt.payload(jwtSecretKey, jwt);
        assertThat(parsedPayload)
                .containsAllEntriesOf(payload);
    }

    @Test
    @DisplayName("authTokenService.genAccessToken 를 통해서 JWT 생성")
    void t4() {
        Member member = memberService.findByUsername("user1").get();
        String accessToken = authTokenService.genAccessToken(member);

        assertThat(accessToken).isNotBlank();
        System.out.println("accessToken = " + accessToken);

        Map<String, Object> parsedPayload = authTokenService.payload(accessToken);

        assertThat(parsedPayload)
                .containsAllEntriesOf(
                        Map.of(
                                "id", member.getId(),
                                "username", member.getUsername(),
                                "name", member.getName()
                        )
                );
    }
}

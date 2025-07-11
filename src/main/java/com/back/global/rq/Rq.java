package com.back.global.rq;

import com.back.domain.member.member.entity.Member;
import com.back.global.security.SecurityUser;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class Rq {
    private final HttpServletRequest req;
    private final HttpServletResponse resp;

    public Member getActor() {
        return Optional.ofNullable(
                        SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                )
                .map(Authentication::getPrincipal)
                .filter(principal -> principal instanceof SecurityUser)
                .map(principal -> (SecurityUser) principal)
                .map(securityUser -> new Member(securityUser.getId(), securityUser.getUsername(), securityUser.getName()))
                .orElse(null);
    }

    public String getHeader(String name, String defaultValue) {
        return Optional.ofNullable(req.getHeader(name))
                .filter(value -> !value.isBlank())
                .orElse(defaultValue);
    }

    public void setHeader(String name, String value) {
        if (value == null) value = "";

        if (value.isBlank()) req.removeAttribute(name);
        else resp.setHeader(name, value);
    }

    public String getCookieValue(String name, String defaultValue) {
        return Optional.ofNullable(req.getCookies())
                .flatMap(cookies -> Arrays.stream(cookies)
                        .filter(cookie -> cookie.getName().equals(name))
                        .map(Cookie::getValue)
                        .filter(value -> !value.isBlank())
                        .findFirst()
                )
                .orElse(defaultValue);
    }

    public void setCookie(String name, String value) {
        if (value == null) value = "";

        ResponseCookie cookie = ResponseCookie.from(name, value)
                .path("/")
                .domain("localhost")
                .sameSite("Strict")
                .secure(true)
                .httpOnly(true)
                .maxAge(value.isBlank() ? 0 : 60 * 60 * 24 * 365 * 10)  // 10년
                .build();

        resp.addHeader("Set-Cookie", cookie.toString());
    }

    public void deleteCookie(String name) {
        setCookie(name, null);
    }
}

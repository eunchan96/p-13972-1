package com.back.global.rq;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.exception.ServiceException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
@RequiredArgsConstructor
public class Rq {
    private final MemberService memberService;
    private final HttpServletRequest req;

    public Member getActor() {
        String header = req.getHeader("Authorization");

        if (header == null || !header.isBlank())
            throw new ServiceException("401-1", "Authorization 헤더가 존재하지 않습니다.");

        if(!header.startsWith("Bearer "))
            throw new ServiceException("401-2", "Authorization 헤더가 Bearer 형식이 아닙니다.");

        String apiKey = header.replace("Bearer ", "");

        return memberService.findByApiKey(apiKey)
                .orElseThrow(() -> new ServiceException("401-3", "API Key가 유효하지 않습니다."));
    }
}

package com.back.domain.member.member.controller;

import com.back.domain.member.member.dto.MemberDto;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.exception.ServiceException;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Tag(name = "ApiV1MemberController", description = "API 회원 컨트롤러")
public class ApiV1MemberController {
    private final MemberService memberService;

    record MemberJoinReqBody(
            @NotBlank(message = "아이디는 필수 입력입니다.")
            @Size(min = 2, max = 30, message = "아이디는 2자 이상 30자 이하로 입력해주세요.")
            String username,

            @NotBlank(message = "비밀번호는 필수 입력입니다.")
            @Size(min = 2, max = 30, message = "비밀번호는 2자 이상 30자 이하로 입력해주세요.")
            String password,

            @NotBlank(message = "닉네임은 필수 입력입니다.")
            @Size(min = 2, max = 30, message = "닉네임은 2자 이상 30자 이하로 입력해주세요.")
            String nickname
    ){};

    @PostMapping
    @Operation(summary = "회원 가입")
    @Transactional
    public RsData<MemberDto> join(@Valid @RequestBody MemberJoinReqBody reqBody) {
        Member member = memberService.join(reqBody.username, reqBody.password, reqBody.nickname);

        return new RsData<>(
                "201-1",
                "%s님 환영합니다. 회원가입이 완료되었습니다.".formatted(member.getName()),
                new MemberDto(member)
        );
    }


    record MemberLoginReqBody(
            @NotBlank(message = "아이디는 필수 입력입니다.")
            @Size(min = 2, max = 30, message = "아이디는 2자 이상 30자 이하로 입력해주세요.")
            String username,

            @NotBlank(message = "비밀번호는 필수 입력입니다.")
            @Size(min = 2, max = 30, message = "비밀번호는 2자 이상 30자 이하로 입력해주세요.")
            String password
    ){};

    record MemberLoginResBody(
            @NotBlank
            MemberDto item,
            @NotBlank
            String apiKey
    ){};

    @PostMapping("/login")
    @Operation(summary = "로그인")
    public RsData<MemberLoginResBody> login(@Valid @RequestBody MemberLoginReqBody reqBody) {
        Member member = memberService.findByUsername(reqBody.username)
                .orElseThrow(() -> new ServiceException("401-1", "존재하지 않는 아이디입니다."));

        if (!member.getPassword().equals(reqBody.password)) {
            throw new ServiceException("401-2", "비밀번호가 일치하지 않습니다.");
        }

        return new RsData<>(
                "200-1",
                "%s님 환영합니다.".formatted(member.getName()),
                new MemberLoginResBody(
                        new MemberDto(member),
                        member.getApiKey()
                )
        );
    }
}

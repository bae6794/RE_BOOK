package com.re_book.user.controller;


import com.re_book.common.auth.JwtTokenProvider;
import com.re_book.common.dto.CommonResDto;
import com.re_book.entity.Member;
import com.re_book.user.dto.LoginRequestDTO;
import com.re_book.user.dto.MemberRequestDTO;
import com.re_book.user.service.MemberService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    @Qualifier("user-template") // RedisTemplate이 여러 개 빈 등록되었을 경우 명시한다.
    private final RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/sign-in")
    public String signIn() {
        return "sign-in";
    }

    @PostMapping("/sign-in")
    public ResponseEntity<CommonResDto> signIn(@RequestBody LoginRequestDTO dto) {
        Member member = memberService.login(dto);

        String token
                = jwtTokenProvider.createToken(member.getId(), member.getRole().toString());
        log.info("token: {}", token);

        String refreshToken
                = jwtTokenProvider.createRefreshToken(member.getId(), member.getRole().toString());

        redisTemplate.opsForValue().set(member.getEmail(), refreshToken, 240, TimeUnit.HOURS);

        // 생성된 토큰 외에 추가로 전달할 정보가 있다면 Map을 사용하는 것이 좋습니다.
        Map<String, Object> logInfo = new HashMap<>();
        logInfo.put("token", token);
        logInfo.put("id", member.getId());

        CommonResDto resDto
                = new CommonResDto(HttpStatus.OK, "로그인 성공!", logInfo);
        return new ResponseEntity<>(resDto, HttpStatus.OK);
    }

    @GetMapping("/log-out")
    public ResponseEntity<CommonResDto> logout() {

        Map<String, Object> logInfo = new HashMap<>();
        CommonResDto resDto
                = new CommonResDto(HttpStatus.OK, "로그아웃 성공!!", logInfo);
        return new ResponseEntity<>(resDto, HttpStatus.OK);
    }

    @GetMapping("/sign-up")
    public String signUp() {
        return "sign-up";
    }

    @PostMapping("/sign-up")
    public String signUp(@RequestBody MemberRequestDTO dto) {
       Member findMember = memberService.findByEmail(dto.getEmail());
        Map<String, Object> logInfo = new HashMap<>();

        if (findMember == null) {
            memberService.save(dto);
            CommonResDto resDto = new CommonResDto(HttpStatus.OK, "회원가입 성공", logInfo);
            return new ResponseEntity<>(resDto, HttpStatus.OK);
            return "redirect:/sign-in";

        } else {
            return "sign-up";
        }
    }

    // 이메일 인증 코드 전송 처리
    @PostMapping("/send-auth-code")
    public ResponseEntity<Void> sendAuthCode(@RequestParam String email, HttpSession session) throws MessagingException {
        String authCode = memberService.sendAuthCode(email); // 인증 코드 발송
        session.setAttribute("sentAuthCode", authCode); // 세션에 인증 코드 저장
        return ResponseEntity.ok().build(); // 성공 응답 반환
    }

    // 인증 코드 확인 처리
    @PostMapping("/verify-auth-code")
    public ResponseEntity<Map<String, Object>> verifyAuthCode(@RequestParam String authCode, HttpSession session) {
        String sentAuthCode = (String) session.getAttribute("sentAuthCode");

        Map<String, Object> response = new HashMap<>();
        response.put("isValid", sentAuthCode != null && sentAuthCode.equals(authCode)); // 입력된 코드와 비교
        return ResponseEntity.ok(response); // 응답 반환
    }
}
package com.re_book.user.controller;


import com.re_book.entity.Member;
import com.re_book.user.dto.LoginRequestDTO;
import com.re_book.user.dto.MemberRequestDTO;
import com.re_book.user.service.LoginResult;
import com.re_book.user.service.MemberService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.re_book.utils.LoginUtils.LOGIN_KEY;


@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/sign-in")
    public String signIn() {
        return "sign-in";
    }

    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@RequestBody LoginRequestDTO dto, HttpServletRequest request) {

        String email = dto.getEmail();
        String password = dto.getPassword();

        Member member = memberService.findByEmail(email);
        LoginResult result = memberService.authenticate(email, password);
        HttpSession session = request.getSession();

        if (result == LoginResult.SUCCESS) {
            session.setAttribute(LOGIN_KEY, member);
            memberService.maintainLoginState(session, email);
            return ResponseEntity.ok().body("로그인 성공"); // JSON으로 성공 응답
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 실패");
        }
    }



    @GetMapping("/log-out")
    public String logout(HttpSession session) {
        // 현재 세션 무효화
        session.invalidate();
        return "redirect:/sign-in"; // 로그아웃 후 리다이렉트
    }

    @GetMapping("/sign-up")
    public String signUp() {
        return "sign-up";
    }

    @PostMapping("/sign-up")
    public String signUp(@RequestParam String email,
                         @RequestParam String nickname,
                         @RequestParam String password
                         ) {
       Member findMember = memberService.findByEmail(email);

        if (findMember == null) {
            memberService.save(new MemberRequestDTO(email, nickname, password));
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
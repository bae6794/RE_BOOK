package com.re_book.profile.controller;

import com.re_book.common.auth.JwtTokenProvider;
import com.re_book.common.auth.TokenUserInfo;
import com.re_book.common.dto.CommonErrorDto;
import com.re_book.common.dto.CommonResDto;
import com.re_book.profile.dto.LikedBooksResponseDTO;
import com.re_book.profile.dto.MyReviewResponseDTO;
import com.re_book.profile.dto.ProfileMemberResponseDTO;
import com.re_book.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/info")
    public ResponseEntity<?> info(@RequestHeader("Authorization") String authorization) {
        Map<String, Object> response = new HashMap<>();

        TokenUserInfo userInfo = jwtTokenProvider.validateAndGetTokenUserInfo(authorization.substring(7));
        if (userInfo != null) {
            ProfileMemberResponseDTO member = profileService.getMyProfile(userInfo.getEmail());
            response.put("member", member);
        } else {
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.UNAUTHORIZED, "Invalid token or user not found"), HttpStatus.UNAUTHORIZED);
        }
        CommonResDto resDto = new CommonResDto(HttpStatus.OK, "회원 정보 조회 완료", response);
        return new ResponseEntity<>(resDto, HttpStatus.OK);
    }

    @GetMapping("/liked-books")
    public ResponseEntity<?> likedBooks(@RequestHeader("Authorization") String authorization,
                                        @PageableDefault(page = 0, size = 5) Pageable page) {
        TokenUserInfo userInfo = jwtTokenProvider.validateAndGetTokenUserInfo(authorization.substring(7));
        if (userInfo == null) {
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.UNAUTHORIZED, "Invalid token or user not found"), HttpStatus.UNAUTHORIZED);
        }

        Page<LikedBooksResponseDTO> likedBooks = profileService.getLikedBooksForMember(userInfo.getEmail(), page);
        Map<String, Object> response = new HashMap<>();
        response.put("likedBooks", likedBooks);

        CommonResDto resDto = new CommonResDto(HttpStatus.OK, "좋아요한 책목록 조회 완료", response);
        return new ResponseEntity<>(resDto, HttpStatus.OK);
    }

    @GetMapping("/my-reviews")
    public String myReviews(@RequestHeader("Authorization") String authorization, Model model,
                            @PageableDefault(page = 0, size = 5) Pageable page) {
        TokenUserInfo userInfo = jwtTokenProvider.validateAndGetTokenUserInfo(authorization.substring(7));
        if (userInfo == null) {
            model.addAttribute("message", "Invalid token or user not found");
            return "redirect:/";
        }

        Page<MyReviewResponseDTO> myReviews = profileService.getMyReviewsForMember(userInfo.getEmail(), page);
        model.addAttribute("myReviews", myReviews);
        return "my-reviews";
    }

    @PostMapping("/change-nickname")
    public String changeNickname(@RequestHeader("Authorization") String authorization,
                                 @RequestParam("newNickname") String newNickname, Model model) {
        TokenUserInfo userInfo = jwtTokenProvider.validateAndGetTokenUserInfo(authorization.substring(7));
        if (userInfo == null) {
            model.addAttribute("message", "Invalid token or user not found");
            return "redirect:/";
        }

        profileService.changeNickname(userInfo.getEmail(), newNickname);
        model.addAttribute("message", "닉네임을 변경했습니다");
        return "redirect:/profile/info";
    }
}


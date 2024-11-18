package com.re_book.profile.controller;


import com.re_book.common.auth.JwtTokenProvider;
import com.re_book.common.dto.CommonResDto;
import com.re_book.profile.dto.LikedBooksResponseDTO;
import com.re_book.profile.dto.MyReviewResponseDTO;
import com.re_book.profile.dto.ProfileMemberResponseDTO;
import com.re_book.profile.service.ProfileService;
import com.re_book.user.dto.LoginUserResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

import static com.re_book.utils.LoginUtils.LOGIN_KEY;


@Controller
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final JwtTokenProvider jwtTokenProvider;


    @GetMapping("/info")
    public ResponseEntity<?> info(HttpServletRequest request) {
//        HttpSession session = request.getSession();
//        LoginUserResponseDTO user = (LoginUserResponseDTO) session.getAttribute(LOGIN_KEY);

        Map<String, Object> response = new HashMap<>();

        if (user != null) {
            ProfileMemberResponseDTO member = profileService.getMyProfile(user.getEmail());
            response.put("member", member);
        }
        CommonResDto resDto = new CommonResDto(HttpStatus.OK, "회원 정보 조회 완료", response);
        return new ResponseEntity<>(resDto, HttpStatus.OK);
    }


//    @GetMapping("/info")
//    public String info(HttpServletRequest request,
//        Model model) {
//        HttpSession session = request.getSession();
//        LoginUserResponseDTO user = (LoginUserResponseDTO) session.getAttribute(LOGIN_KEY);
//
//        if (user != null) {
//            ProfileMemberResponseDTO member = profileService.getMyProfile(user.getEmail());
//            model.addAttribute("member", member);
//        }
//        return "member-info";
//    }

    @GetMapping("/liked-books")
    public ResponseEntity<?> likedBooks(HttpServletRequest request,
                                        @PageableDefault(page = 0, size = 5) Pageable page) {
//        HttpSession session = request.getSession();
//        LoginUserResponseDTO user = (LoginUserResponseDTO) session.getAttribute(LOGIN_KEY);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND).header("Location", "/").build();
        }

        Page<LikedBooksResponseDTO> likedBooks = profileService.getLikedBooksForMember(user.getEmail(), page);
        Map<String, Object> response = new HashMap<>();
        response.put("likedBooks", likedBooks);

        CommonResDto resDto = new CommonResDto(HttpStatus.OK, "좋아요한 책목록 조회 완료", response);
        return new ResponseEntity<>(resDto, HttpStatus.OK);
    }


//    @GetMapping("/liked-books")
//    public String likedBooks(HttpServletRequest request,
//                             Model model,
//                             @PageableDefault(page = 0, size = 5) Pageable page) {
//        HttpSession session = request.getSession();
//        LoginUserResponseDTO user = (LoginUserResponseDTO) session.getAttribute(LOGIN_KEY);
//
//        if (user != null) {
//            Page<LikedBooksResponseDTO> likedBooks = profileService.getLikedBooksForMember(user.getEmail(),page);
//            if (likedBooks != null) {
//                System.out.println("likedBooks = " + likedBooks);
//            }
//            model.addAttribute("likedBooks", likedBooks);
//        } else {
//            return "redirect:/";
//        }
//
//        return "liked-books";
//    }

    @GetMapping("/my-reviews")
    public String myReviews(HttpServletRequest request,
                            Model model,
                            @PageableDefault(page = 0, size = 5) Pageable page) {
        HttpSession session = request.getSession();
        LoginUserResponseDTO user = (LoginUserResponseDTO) session.getAttribute(LOGIN_KEY);
        if (user != null) {
            Page<MyReviewResponseDTO> myReviews = profileService.getmyReviewsForMember(
                    user.getEmail(), page);
            model.addAttribute("myReviews", myReviews);
        }

        return "my-reviews";
    }

    @GetMapping("/liked-books")
    public ResponseEntity<?> likedBooks(HttpServletRequest request,
                                        @PageableDefault(page = 0, size = 5) Pageable page) {
        HttpSession session = request.getSession();
        LoginUserResponseDTO user = (LoginUserResponseDTO) session.getAttribute(LOGIN_KEY);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND).header("Location", "/").build();
        }

        Page<LikedBooksResponseDTO> likedBooks = profileService.getLikedBooksForMember(user.getEmail(), page);
        Map<String, Object> response = new HashMap<>();
        response.put("likedBooks", likedBooks);

        CommonResDto resDto = new CommonResDto(HttpStatus.OK, "좋아요한 책목록 조회 완료", response);
        return new ResponseEntity<>(resDto, HttpStatus.OK);
    }


    @PostMapping("/change-nickname")
    public String changeNickname(HttpServletRequest request,
                                 @RequestParam("newNickname") String newNickname, Model model) {
        HttpSession session = request.getSession();
        LoginUserResponseDTO user = (LoginUserResponseDTO) session.getAttribute(LOGIN_KEY);
        if (user != null) {
            profileService.changeNickname(user.getEmail(), newNickname);
            model.addAttribute("message", "닉네임을 변경했습니다");
        }
        return "redirect:/profile/info";

    }

}

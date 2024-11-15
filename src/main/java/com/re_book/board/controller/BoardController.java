package com.re_book.board.controller;

import com.re_book.board.dto.response.BookDetailResponseDTO;
import com.re_book.board.dto.response.DetailPageResponseDTO;
import com.re_book.board.dto.response.ReviewResponseDTO;
import com.re_book.board.service.BoardService;
import com.re_book.board.service.DetailService;
import com.re_book.board.service.ReviewService;
import com.re_book.common.auth.JwtTokenProvider;
import com.re_book.common.auth.TokenUserInfo;
import com.re_book.common.dto.CommonErrorDto;
import com.re_book.common.dto.CommonResDto;
import com.re_book.user.dto.LoginUserResponseDTO;
import com.re_book.user.dto.UserResDto;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.re_book.utils.LoginUtils.LOGIN_KEY;


@RestController
@RequestMapping("/board")
@RequiredArgsConstructor
@Slf4j
public class BoardController {

    private final BoardService boardService;
    private final DetailService detailService;
    private final ReviewService reviewService;
    private final JwtTokenProvider jwtTokenProvider;


    @GetMapping("/list")
    public ResponseEntity<?> list(@PageableDefault(size = 9) Pageable page,
                       @RequestParam(required = false) String sort,
                       @RequestParam(required = false) String query) {
        Map<String, Object> response = new HashMap<>();


        // page 설정에 맞춰 북 목록을 Map에 저장하겠다.
        Page<BookDetailResponseDTO> bookPage;

        if (query != null && !query.trim().isEmpty()) {
            bookPage = getSortedBookPageForSearch(sort, page, query);
        } else {
            bookPage = getSortedBookPage(sort, page);
        }

        response.put("bList", bookPage.getContent());
        response.put("maker", bookPage);
        response.put("sort", sort);
        response.put("query", query);
        CommonResDto resDto
                = new CommonResDto(HttpStatus.OK, "책목록 조회 완료", response);
        return new ResponseEntity<>(resDto, HttpStatus.OK); // 성공 시 OK 상태 코드와 함께 반환
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<?> detailPage(@PathVariable String id,
                                        @PageableDefault(page = 0, size = 10) Pageable page,
                                        @RequestHeader("Authorization") String authorization) {
        log.info("Fetching detail for book id: {}", id);
        boolean tokenFlag = false;

        Map<String, Object> response = new HashMap<>();

        String token = authorization.substring(7);  // 'Bearer '를 제거하여 토큰만 추출

        TokenUserInfo userInfo = null;
        try {
            userInfo = jwtTokenProvider.validateAndGetTokenUserInfo(token); // 토큰 유효성 검사 및 사용자 정보 추출
        } catch (ExpiredJwtException e) {
            CommonErrorDto dto = new CommonErrorDto(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다.");
            return new ResponseEntity<>(dto, HttpStatus.UNAUTHORIZED);
        } catch (UnsupportedJwtException e) {
            CommonErrorDto dto = new CommonErrorDto(HttpStatus.UNAUTHORIZED, "지원되지 않는 토큰 형식입니다.");
            return new ResponseEntity<>(dto, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            CommonErrorDto dto = new CommonErrorDto(HttpStatus.UNAUTHORIZED, "토큰이 유효하지 않거나 만료되었습니다.");
            return new ResponseEntity<>(dto, HttpStatus.UNAUTHORIZED);
        }

        // 책 정보와 함께 좋아요 상태 및 좋아요 수 가져오기
        DetailPageResponseDTO bookDetail = detailService.getBookDetail(id, userInfo.getId());

        boolean isLiked = bookDetail.isLiked(); // 사용자의 좋아요 상태

        int likeCount = bookDetail.getLikeCount(); // 좋아요 수
        Page<ReviewResponseDTO> reviewPage = reviewService.getReviewList(id, page);

        // 책 정보가 제대로 전달되는지 로그로 확인
        log.info("Book detail: {}", bookDetail);

        // 모델에 필요한 정보 추가
        response.put("book", bookDetail);
        response.put("isLiked", isLiked); // 좋아요 상태 추가
        response.put("likeCount", likeCount); // 좋아요 수 추가
        response.put("reviewList", reviewPage.getContent());
        response.put("page", reviewPage);
        response.put("user", userInfo.getId());  // 추가된 부분

        CommonResDto resDto
                = new CommonResDto(HttpStatus.OK, "디테일페이지 조회 완료", response);
        return new ResponseEntity<>(resDto, HttpStatus.OK); // 성공 시 OK 상태 코드와 함께 반환
    }


    @PostMapping("/detail/{bookId}/toggle-like")
    @ResponseBody
    public ResponseEntity<?> toggleLike(@PathVariable String bookId, @RequestHeader("Authorization") String authorization) {
        log.info("/toggle-like: POST, {}", bookId);

        Map<String, Object> response = new HashMap<>();

        // Authorization 헤더가 없거나, 'Bearer ' 접두어가 없는 경우 처리
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            CommonErrorDto dto = new CommonErrorDto(HttpStatus.BAD_REQUEST, "Authorization 헤더가 없거나 잘못된 형식입니다.");
            return new ResponseEntity<>(dto, HttpStatus.BAD_REQUEST);
        }

        String token = authorization.substring(7);  // 'Bearer '를 제거하여 토큰만 추출

        TokenUserInfo userInfo = null;
        try {
            userInfo = jwtTokenProvider.validateAndGetTokenUserInfo(token); // 토큰 유효성 검사 및 사용자 정보 추출
        } catch (ExpiredJwtException e) {
            CommonErrorDto dto = new CommonErrorDto(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다.");
            return new ResponseEntity<>(dto, HttpStatus.UNAUTHORIZED);
        } catch (UnsupportedJwtException e) {
            CommonErrorDto dto = new CommonErrorDto(HttpStatus.UNAUTHORIZED, "지원되지 않는 토큰 형식입니다.");
            return new ResponseEntity<>(dto, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            CommonErrorDto dto = new CommonErrorDto(HttpStatus.UNAUTHORIZED, "토큰이 유효하지 않거나 만료되었습니다.");
            return new ResponseEntity<>(dto, HttpStatus.UNAUTHORIZED);
        }

        // 좋아요 토글 및 카운트 업데이트
        boolean isLiked = detailService.toggleLike(bookId, userInfo.getId());

        int likeCount = detailService.getBookDetail(bookId, userInfo.getId()).getLikeCount(); // 좋아요 수 업데이트
        response.put("success", true);
        response.put("isLiked", isLiked);
        response.put("likeCount", likeCount); // 좋아요 수를 응답에 포함

        CommonResDto resDto
                = new CommonResDto(HttpStatus.OK, "좋아요 토글 성공!", response);
        return new ResponseEntity<>(resDto, HttpStatus.OK); // 성공 시 OK 상태 코드와 함께 반환
    }

    // 종속성 메서드
    private Page<BookDetailResponseDTO> getSortedBookPage(String sort, Pageable pageable) {
        if (sort == null) {
            return boardService.getBookList(pageable);
        }

        return switch (sort) {
            case "likeCount" -> boardService.getOrderLikeDesc(pageable);
            case "reviewCount" -> boardService.getOrderReviewDesc(pageable);
            case "rating" -> boardService.getOrderRatingDesc(pageable);
            default -> boardService.getBookList(pageable);
        };
    }

    // 종속성 메서드
    private Page<BookDetailResponseDTO> getSortedBookPageForSearch(String sort, Pageable page,
                                                                   String query) {
        Page<BookDetailResponseDTO> books;

        if (sort == null) {
            books = boardService.searchByName(page, query);
        } else {
            books = switch (sort) {
                case "likeCount" -> boardService.searchByNameOrderByLikeDesc(page, query);
                case "reviewCount" -> boardService.searchBookByNameOrderByReviewDesc(page, query);
                case "rating" -> boardService.searchBookByNameOrderByRatingDesc(page, query);
                default -> boardService.searchByName(page, query);
            };
        }

        return books;
    }

}

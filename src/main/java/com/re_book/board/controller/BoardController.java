package com.re_book.board.controller;

import com.re_book.board.service.BoardService;
import com.re_book.common.dto.CommonResDto;
import com.re_book.board.dto.request.BookSearchDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class BoardController {
    private final BoardService boardService;

    @GetMapping("/list")
    public ResponseEntity<?> listBooks(BookSearchDto searchDto, Pageable pageable) {
        log.info("/list: GET, dto: {}", searchDto);
        log.info("/list: GET, pageable={}", pageable);
        Page<BookSearchDto> dtoList = boardService.bookList(searchDto, pageable);

        CommonResDto resDto
                = new CommonResDto(HttpStatus.OK, "도서 리스트 조회 성공", dtoList);
        return new ResponseEntity<>(resDto, HttpStatus.OK);
    }

}

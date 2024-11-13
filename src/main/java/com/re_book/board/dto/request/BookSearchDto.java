package com.re_book.board.dto;

import lombok.*;

@Setter @Getter @ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookSearchDto {

    private String Category;
    private String searchName;

}

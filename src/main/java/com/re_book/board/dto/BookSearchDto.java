package com.re_book.list.dto;

import jdk.jfr.Category;
import lombok.*;

@Setter @Getter @ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookSearchDto {

    private String Category;
    private String searchName;

}

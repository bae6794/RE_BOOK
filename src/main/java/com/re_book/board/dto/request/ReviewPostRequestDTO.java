package com.re_book.board.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class ReviewPostRequestDTO {

    private String memberUuid;
    private String nickName;

    @NotBlank
    @Size(min = 1, max = 300)
    private final String content;
    private int rating;

}

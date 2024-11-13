package com.re_book.board.dto.response;

import com.re_book.entity.Review;
import lombok.*;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@Builder
@Setter

public class ReviewResponseDTO {
    private final String id;
    private int rating;
    private final String content;
    private final String memberName;
    private String memberUuid;

    private int reviewCount;




    public ReviewResponseDTO(Review review) {
        this.id = review.getId();
        this.rating = review.getRating();
        this.content = review.getContent();
        this.memberName = review.getMember().getNickname();
        this.memberUuid = review.getMember().getUuid();
        this.reviewCount = review.getBook().getReviewCount();
    }


}



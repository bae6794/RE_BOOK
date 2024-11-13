package com.re_book.profile.entity;

import com.re_book.entity.Book;
import com.re_book.user.entity.Member;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "book_likes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter @Getter
public class BookLike {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "book_uuid")
    private Book book;

    @ManyToOne
    @JoinColumn(name = "member_uuid")
    private Member member;

}
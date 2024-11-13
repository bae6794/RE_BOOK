package com.re_book.repository;



import com.re_book.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, String> {
    Member findByEmail(String email);

    Member findBySessionId(String sessionId);


    boolean existsByEmail(String email);

}
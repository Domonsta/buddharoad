// src/main/java/com/buddharoad/repository/Member/MemberRepository.java
package com.buddharoad.repository.Member;

import com.buddharoad.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // ⭐ JpaSpecificationExecutor 임포트
import org.springframework.stereotype.Repository; // @Repository 어노테이션 추가

import java.util.Optional;

@Repository // ⭐ @Repository 어노테이션 추가
public interface MemberRepository extends JpaRepository<Member, Long>, JpaSpecificationExecutor<Member> { // ⭐ JpaSpecificationExecutor 추가!
    // loginId로 회원 존재 여부 확인
    boolean existsByLoginId(String loginId);
    // email로 회원 존재 여부 확인
    boolean existsByEmail(String email);
    // username(닉네임)으로 회원 존재 여부 확인
    boolean existsByUsername(String username);

    // loginId로 회원 조회
    Optional<Member> findByLoginId(String loginId);

    // username(닉네임)으로 회원 조회
    Optional<Member> findByUsername(String username);

    Optional<Member> findByMemberNo(Long memberNo);

    // ⭐ 추가: 회원 정보 수정 시 본인 제외하고 이메일 중복 확인
    boolean existsByEmailAndMemberNoNot(String email, Long memberNo);

    // ⭐ 추가: 회원 정보 수정 시 본인 제외하고 닉네임 중복 확인
    boolean existsByUsernameAndMemberNoNot(String username, Long memberNo);
}
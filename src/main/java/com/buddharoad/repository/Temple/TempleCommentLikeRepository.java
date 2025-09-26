package com.buddharoad.repository.Temple; // 💡 패키지 경로를 temple로 변경

import com.buddharoad.domain.TempleCommentLike;
import com.buddharoad.domain.TempleCommentLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface TempleCommentLikeRepository extends JpaRepository<TempleCommentLike, TempleCommentLikeId> {

    // ✅ 특정 회원이 특정 댓글을 '좋아요' 했는지 확인
    boolean existsByMemberNoAndTempleCommentId(Long memberNo, Long templeCommentId);

    // ✅ 특정 회원이 '좋아요' 한 모든 댓글 목록 조회 (마이페이지용)
    List<TempleCommentLike> findByMemberNo(Long memberNo);

    // ✅ '좋아요' 토글을 위한 '좋아요' 정보 조회
    Optional<TempleCommentLike> findByMemberNoAndTempleCommentId(Long memberNo, Long templeCommentId);

    // ✅ 특정 '좋아요' 정보 삭제
    @Transactional
    void deleteByMemberNoAndTempleCommentId(Long memberNo, Long templeCommentId);

}
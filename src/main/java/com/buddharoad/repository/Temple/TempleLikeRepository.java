package com.buddharoad.repository.Temple; // 💡 패키지 경로를 temple로 변경

import com.buddharoad.domain.TempleLike;
import com.buddharoad.domain.TempleLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface TempleLikeRepository extends JpaRepository<TempleLike, TempleLikeId> {

    // ✅ 특정 회원이 특정 사찰을 '좋아요' 했는지 확인
    boolean existsByMemberNoAndTempleId(Long memberNo, Long templeId);

    // ✅ 특정 회원이 '좋아요' 한 모든 사찰 목록 조회 (마이페이지용)
    List<TempleLike> findByMemberNo(Long memberNo);

    // ✅ '좋아요' 토글을 위한 '좋아요' 정보 조회
    Optional<TempleLike> findByMemberNoAndTempleId(Long memberNo, Long templeId);

    // ✅ 특정 '좋아요' 정보 삭제
    @Transactional
    void deleteByMemberNoAndTempleId(Long memberNo, Long templeId);

}
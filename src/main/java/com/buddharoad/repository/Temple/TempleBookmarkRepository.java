// src/main/java/com/buddharoad/repository/Temple/TempleBookmarkRepository.java
package com.buddharoad.repository.Temple;

import com.buddharoad.domain.TempleBookmark;
import com.buddharoad.domain.TempleBookmarkId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional; // 💡 트랜잭션 임포트 추가

import java.util.List;
import java.util.Optional; // 💡 Optional 임포트 추가

@Repository
public interface TempleBookmarkRepository extends JpaRepository<TempleBookmark, TempleBookmarkId> {

    // 💡 특정 회원이 특정 사찰을 찜했는지 확인하는 메서드 (토글 기능에 필요!)
    boolean existsByMemberNoAndTempleId(Long memberNo, Long templeId);

    // 💡 특정 회원이 찜한 모든 사찰 목록을 조회하는 메서드 (마이페이지용)
    List<TempleBookmark> findByMemberNo(Long memberNo);

    // 💡 토글 기능을 위해 Optional로 조회하는 메서드 추가
    Optional<TempleBookmark> findByMemberNoAndTempleId(Long memberNo, Long templeId);

    // 💡 찜하기 삭제 시 사용하는 메서드 (이름이 명확해서 좋아!)
    @Transactional // 삭제는 쓰기 작업이므로 @Transactional이 필요해
    void deleteByMemberNoAndTempleId(Long memberNo, Long templeId);
}